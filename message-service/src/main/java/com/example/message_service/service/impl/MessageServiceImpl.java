package com.example.message_service.service.impl;

import com.example.message_service.client.CryptoFeignClient;
import com.example.message_service.client.UserKeyFeignClient;
import com.example.message_service.dto.crypto.AESEncryptionRequest;
import com.example.message_service.dto.crypto.RSAEncryptKeyRequest;
import com.example.message_service.entity.ConversationDocument;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.error.AuthErrorCode;
import com.example.message_service.error.InvalidErrorCode;
import com.example.message_service.error.NotExistedErrorCode;
import com.example.message_service.exception.AppException;
import com.example.message_service.kafka.producer.ProducerKafkaMessage;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseBlockMessageRepository;
import com.example.message_service.repository.ICouchbaseConversationRepository;
import com.example.message_service.repository.ICouchbaseMessageRepository;
import com.example.message_service.service.interfaces.IMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;

@Service
public class MessageServiceImpl implements IMessageService {
    private final ICouchbaseConversationRepository ICouchbaseConversationRepository;
    private final ICouchbaseMessageRepository ICouchbaseMessageRepository;
    private final ProducerKafkaMessage producerKafkaMessage;
    private final ICouchbaseBlockMessageRepository ICouchbaseBlockMessageRepository;
    private final ProducerLog producerLog;
    private final CryptoFeignClient cryptoFeignClient;
    private final UserKeyFeignClient userKeyFeignClient;
    public MessageServiceImpl(ICouchbaseConversationRepository ICouchbaseConversationRepository,
                              ICouchbaseMessageRepository ICouchbaseMessageRepository,
                              ProducerKafkaMessage producerKafkaMessage,
                              ICouchbaseBlockMessageRepository ICouchbaseBlockMessageRepository,
                              ProducerLog producerLog,
                              CryptoFeignClient cryptoFeignClient,
                              UserKeyFeignClient userKeyFeignCliente,
                              ProducerLog logProducer) {
        this.ICouchbaseConversationRepository = ICouchbaseConversationRepository;
        this.ICouchbaseMessageRepository = ICouchbaseMessageRepository;
        this.producerKafkaMessage = producerKafkaMessage;
        this.ICouchbaseBlockMessageRepository = ICouchbaseBlockMessageRepository;
        this.producerLog = producerLog;
        this.cryptoFeignClient = cryptoFeignClient;
        this.userKeyFeignClient = userKeyFeignCliente;
    }

    @Override
    public List<MessageDocument> getAllMessagesOfAConversation(String conversationId, String token) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            ConversationDocument conversation = ICouchbaseConversationRepository.findById(conversationId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.CONVERSATION_NOT_FOUND));

            if (!conversation.getParticipants().contains(userId))
                throw new AppException(NotExistedErrorCode.USER_NOT_IN_CONVERSATION);

            List<MessageDocument> messages = ICouchbaseMessageRepository.findByConversationId(conversationId);
            messages.removeIf(msg -> msg.getDeletedBy().contains(userId));

            String privateKeyBase64 = userKeyFeignClient.getPrivateKey(token);

            for (MessageDocument msg : messages) {
                try {
                    String encryptedKeyForUser = msg.getEncryptedKeys() != null ? msg.getEncryptedKeys().get(userId) : null;
                    if (encryptedKeyForUser == null) {
                        msg.setText(null);
                        continue;
                    }
                    String aesKeyBase64 = cryptoFeignClient.decryptAESKeyRSA(
                            new com.example.message_service.dto.crypto.RSADecryptKeyRequest(encryptedKeyForUser, privateKeyBase64)
                    );
                    String plainText = cryptoFeignClient.decryptAES(
                            new com.example.message_service.dto.crypto.AESDecryptionRequest(msg.getCipherText(), aesKeyBase64)
                    );
                    msg.setText(plainText);
                } catch (Exception ex) {
                    msg.setText(null);
                    producerLog.sendLog("message-service", "ERROR", "[Decrypt] Failed message " + msg.getIdMessage() + ": " + ex.getMessage());
                }
            }
            return messages;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.GET_MESSAGES_FAILED);
        }
    }

    @Override
    public MessageDocument sendMessage(MessageDocument message,String token,String userId) {
        try {
            if (userId == null) throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);
            System.out.println("userId: "+userId + "messageSenderId: "+message.getSenderId());
            if (!userId.equals(message.getSenderId())) {
                producerLog.sendLog("message-service", "ERROR",
                        "[Send Message] SenderId " + message.getSenderId() + " does not match authenticated user " + userId);
                throw new AppException(InvalidErrorCode.SENDER_ID_MISMATCH);
            }

            ConversationDocument conversation = ICouchbaseConversationRepository.findById(message.getConversationId())
                    .orElseThrow(() -> {
                        producerLog.sendLog("message-service", "ERROR", "[Send Message] Conversation not found");
                        return new AppException(NotExistedErrorCode.CONVERSATION_NOT_FOUND);
                    });

            // Kiểm tra block
            for (String participant : conversation.getParticipants()) {
                if (!participant.equals(userId) && ICouchbaseBlockMessageRepository.isBlocked(userId, participant))
                    throw new AppException(AuthErrorCode.BLOCKED_BY_USER);
                if (!participant.equals(userId) && ICouchbaseBlockMessageRepository.isBlocked(participant, userId))
                    throw new AppException(AuthErrorCode.BLOCK_BY_USER);
            }

            // Tạo AES key
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey aesKey = keyGen.generateKey();
            String base64AESKey = Base64.getEncoder().encodeToString(aesKey.getEncoded());

            // Mã hóa nội dung AES
            AESEncryptionRequest aesRequest = new AESEncryptionRequest();
            aesRequest.setPlainText(message.getText());
            aesRequest.setKey(base64AESKey);
            String cipherText = cryptoFeignClient.encryptAES(aesRequest);

            // Mã hóa AES key cho tất cả participants (bao gồm sender)
            Map<String, String> encryptedKeys = new java.util.HashMap<>();
            for (String participant : conversation.getParticipants()) {
                String publicKey = userKeyFeignClient.getPublicKey(participant);
                RSAEncryptKeyRequest rsaRequest = new RSAEncryptKeyRequest(base64AESKey, publicKey);
                String encryptedKey = cryptoFeignClient.encryptAESKeyRSA(rsaRequest);
                encryptedKeys.put(participant, encryptedKey);
            }

            // Gán vào message
            message.setCipherText(cipherText);
            message.setEncryptedKeys(encryptedKeys);
            message.setEncryptionAlg("AES/RSA");
            message.setTimestamp(Instant.now().toEpochMilli());
            message.setIsReadBy(Collections.singletonList(userId));
            message.setDeletedBy(Collections.emptyList());

            // Lưu tin nhắn và cập nhật conversation
            if (message.getIdMessage() == null || message.getIdMessage().isEmpty()) {
                message.setIdMessage(UUID.randomUUID().toString());
            }

            MessageDocument savedMessage = ICouchbaseMessageRepository.save(message);
            conversation.setLastMessage(savedMessage);
            conversation.setUpdatedAt(Instant.now().toEpochMilli());
            ICouchbaseConversationRepository.save(conversation);

            producerKafkaMessage.publishMessage(savedMessage);
            return savedMessage;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            producerLog.sendLog("message-service", "ERROR",
                    "[Send Message] Failed: " + e.getMessage());
            throw new AppException(AuthErrorCode.SEND_MESSAGE_FAILED);
        }
    }


    @Override
    public void markMessageAsRead(String messageId, String userId) {
        try {
            MessageDocument message = ICouchbaseMessageRepository.findById(messageId)
                    .orElseThrow(() -> {
                        producerLog.sendLog("message-service", "ERROR", "[Mark Message] Message not found");
                        return new AppException(NotExistedErrorCode.MESSAGE_NOT_FOUND);
                    });
            if (!message.getIsReadBy().contains(userId)) {
                message.getIsReadBy().add(userId);
                ICouchbaseMessageRepository.save(message);
                producerLog.sendLog("message-service", "DEBUG", "[Mark Message] Successfully marked message as read by " + userId);
            } else {
                producerLog.sendLog("message-service", "DEBUG", "[Mark Message] Message already read by " + userId);
            }
        } catch (AppException e) {
            // Rethrow AppException to preserve specific error details
            throw e;
        } catch (Exception e) {
            producerLog.sendLog("message-service", "ERROR", "[Mark Message] Failed to mark message as read by " + userId + ": " + e.getMessage());
            throw new AppException(AuthErrorCode.MARK_AS_READ_FAILED);
        }
    }

    @Override
    public void deleteMessageForUser(String messageId, String userId)
    {
        try {
            producerLog.sendLog("message-service", "INFO", "[Delete Message] Deleting message " + messageId + " for user " + userId);
            MessageDocument message = ICouchbaseMessageRepository.findById(messageId)
                    .orElseThrow(() -> {
                        producerLog.sendLog("message-service", "ERROR", "[Delete Message] Message not found");
                        return new AppException(NotExistedErrorCode.MESSAGE_NOT_FOUND);
                    });
            //Check user thuộc đoạn chat
            ConversationDocument conversation = ICouchbaseConversationRepository.findById(message.getConversationId())
                    .orElseThrow(() -> {
                        producerLog.sendLog("message-service", "ERROR", "[Delete Message] Conversation not found for message " + messageId);
                        return new AppException(NotExistedErrorCode.CONVERSATION_NOT_FOUND);
                    });
            if (!conversation.getParticipants().contains(userId)) {
                producerLog.sendLog("message-service", "WARN", "[Delete Message] User " + userId + " không thuộc cuộc trò chuyện " + conversation.getIdConversation());
                throw new AppException(NotExistedErrorCode.USER_NOT_IN_CONVERSATION);
            }
            //Thực hiện xóa mềm nếu hợp lệ
            if (!message.getDeletedBy().contains(userId)) {
                message.getDeletedBy().add(userId);
                ICouchbaseMessageRepository.save(message);
                producerLog.sendLog("message-service", "DEBUG", "[Delete Message] Successfully deleted message " + messageId + " for user " + userId);
            } else {
                producerLog.sendLog("message-service", "INFO", "[Delete Message] Message " + messageId + " already deleted by " + userId);
            }
        } catch (AppException e) {
            throw e;

        } catch (Exception e) {
            producerLog.sendLog("message-service", "ERROR", "[Delete Message] Failed to delete message " + messageId + ": " + e.getMessage());
            throw new AppException(AuthErrorCode.DELETE_MESSAGE_FAILED);
        }
    }
}