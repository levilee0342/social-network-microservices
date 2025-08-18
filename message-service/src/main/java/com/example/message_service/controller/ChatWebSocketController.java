package com.example.message_service.controller;

import com.example.message_service.dto.request.ChatFetchMessagesRequest;
import com.example.message_service.dto.response.ChatFetchMessagesResponse;
import com.example.message_service.dto.websocket.ChatDeleteMessageRequest;
import com.example.message_service.dto.websocket.ChatMarkReadRequest;
import com.example.message_service.dto.websocket.ChatSendMessageRequest;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.service.impl.MessageServiceImpl;
import com.example.message_service.utils.WebSocketSessionRegistry;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

import java.util.List;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageServiceImpl messageService;
    private final WebSocketSessionRegistry sessionRegistry;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   MessageServiceImpl messageService,
                                   WebSocketSessionRegistry sessionRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.sessionRegistry = sessionRegistry;
    }

    @MessageMapping("/chat.fetchAllMessages")
    public void fetchAllMessages(@Payload ChatFetchMessagesRequest request,
                                 SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String userId = sessionRegistry.getUser(sessionId);
        String token = accessor.getFirstNativeHeader("Authorization");
        List<MessageDocument> messages = messageService.getAllMessagesOfAConversation(
                request.getConversationId(), token);

        // Tạo response
        ChatFetchMessagesResponse resp = new ChatFetchMessagesResponse();
        resp.setType("fetchAllMessagesResult");
        resp.setConversationId(request.getConversationId());
        resp.setMessages(messages);

        // Gửi lại cho user vừa request
        messagingTemplate.convertAndSendToUser(userId, "/queue/messages", resp);
    }


    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatSendMessageRequest request,
                            SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String userId = sessionRegistry.getUser(sessionId);
        String token = accessor.getFirstNativeHeader("Authorization");

        // Xây dựng message document
        MessageDocument message = new MessageDocument();
        message.setConversationId(request.getConversationId());
        message.setSenderId(userId);
        message.setText(request.getText());

        // Gửi tin nhắn (business logic như cũ)
        MessageDocument saved = messageService.sendMessage(message, token, userId);

        // Đẩy về từng participant
        saved.getEncryptedKeys().forEach((participant, key) -> {
            messagingTemplate.convertAndSendToUser(participant, "/queue/messages", saved);
        });
    }

    @MessageMapping("/chat.markRead")
    public void markAsRead(@Payload ChatMarkReadRequest request,
                           SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String userId = sessionRegistry.getUser(sessionId);

        messageService.markMessageAsRead(request.getMessageId(), userId);
    }

    @MessageMapping("/chat.delete")
    public void deleteMessage(@Payload ChatDeleteMessageRequest request, SimpMessageHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String userId = sessionRegistry.getUser(sessionId);

        messageService.deleteMessageForUser(request.getMessageId(), userId);
    }

}
