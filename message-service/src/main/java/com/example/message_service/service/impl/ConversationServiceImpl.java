package com.example.message_service.service.impl;

import com.example.message_service.entity.AnonymousConversationDocument;
import com.example.message_service.entity.ConversationDocument;
import com.example.message_service.error.AuthErrorCode;
import com.example.message_service.error.InvalidErrorCode;
import com.example.message_service.error.NotExistedErrorCode;
import com.example.message_service.exception.AppException;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseAnonymousConversationRepository;
import com.example.message_service.repository.ICouchbaseConversationRepository;
import com.example.message_service.service.interfaces.IAuthManagerService;
import com.example.message_service.service.interfaces.IConversationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationServiceImpl implements IConversationService {

    private final IAuthManagerService authManagerService;
    private final ICouchbaseConversationRepository ICouchbaseConversationRepository;
    private final ICouchbaseAnonymousConversationRepository ICouchbaseAnonymousConversationRepository;
    private final ProducerLog producerLog;

    public ConversationServiceImpl(IAuthManagerService authManagerService,
                                   ICouchbaseConversationRepository ICouchbaseConversationRepository,
                                   ICouchbaseAnonymousConversationRepository ICouchbaseAnonymousConversationRepository,
                                   ProducerLog producerLog) {
        this.authManagerService = authManagerService;
        this.ICouchbaseConversationRepository = ICouchbaseConversationRepository;
        this.ICouchbaseAnonymousConversationRepository = ICouchbaseAnonymousConversationRepository;
        this.producerLog = producerLog;
    }

    @Override
    public ConversationDocument createConversation(ConversationDocument conversation, HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        String creatorId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            List<String> participants = conversation.getParticipants();
            if (participants == null || participants.size() < 2) {
                throw new AppException(AuthErrorCode.CONVERSATION_INVALID_PARTICIPANTS);
            }
            // Assign random ID based on group type
            String prefix = participants.size() > 2 ? "group::" : "conv::";
            String id = prefix + UUID.randomUUID();
            conversation.setIdConversation(id);
            // Check if the creator is in the participants list
            if (!participants.contains(creatorId)) {
                producerLog.sendLog(
                        "message-service",
                        "WARN",
                        "[Conversation] Creator (userId=" + creatorId + ") not in participants list");
                throw new AppException(InvalidErrorCode.CREATOR_NOT_IN_PARTICIPANTS);
            }
            // Validate each user
            for (String userId : participants) {
                boolean userExists = authManagerService.checkExitsUser(request);
                if (!userExists) {
                    producerLog.sendLog(
                            "message-service",
                            "WARN",
                            "[Conversation] User not found: " + userId);
                    throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);
                }
            }
            conversation.setUpdatedAt(Instant.now().toEpochMilli());
            ConversationDocument savedConversation = ICouchbaseConversationRepository.save(conversation);
            producerLog.sendLog(
                    "message-service",
                    "DEBUG",
                    "[Conversation] Successfully created conversation with id: " + id);
            return savedConversation;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    "[Conversation] Failed to create conversation: " + e.getMessage());
            throw new AppException(AuthErrorCode.CREATE_CONVERSATION_FAILED);
        }
    }

    @Override
    public AnonymousConversationDocument  createAnonymousConversation(List<String> participants) {
        AnonymousConversationDocument doc = new AnonymousConversationDocument();
        doc.setIdConversation("anon::" + UUID.randomUUID());
        doc.setParticipants(participants);
        long now = Instant.now().toEpochMilli();
        doc.setCreatedAt(now);
        doc.setUpdatedAt(now);
        return ICouchbaseAnonymousConversationRepository.save(doc);
    }
    @Override
    public List<ConversationDocument> getAllConversationsOfUser(HttpServletRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication() != null
                ? (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        try {
            // Lấy tất cả conversation mà user tham gia
            List<ConversationDocument> conversations = ICouchbaseConversationRepository.findAllByParticipantsContains(userId);
            producerLog.sendLog(
                    "message-service",
                    "DEBUG",
                    "[Conversation] Found " + conversations.size() + " conversations for userId=" + userId);

            return conversations;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    "[Conversation] Failed to fetch conversations for userId=" + userId + " : " + e.getMessage());
            throw new AppException(AuthErrorCode.FETCH_CONVERSATION_FAILED);
        }
    }

}
