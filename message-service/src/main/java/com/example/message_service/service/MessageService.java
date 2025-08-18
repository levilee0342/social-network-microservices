package com.example.message_service.service;

import com.example.message_service.entity.MessageDocument;
import com.example.message_service.service.interfaces.IMessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final IMessageService messageService;

    public MessageService(IMessageService messageService) {
        this.messageService = messageService;
    }

    public MessageDocument sendMessage(MessageDocument message, String token, String userId) {
        return messageService.sendMessage(message, token, userId);
    }

    public void markMessageAsRead(String messageId, String userId) {
        messageService.markMessageAsRead(messageId, userId);
    }

    public void deleteMessageForUser(String messageId, String  userId) {
        messageService.deleteMessageForUser(messageId, userId);
    }

    public List<MessageDocument> getAllMessagesOfAConversation(String conversationId, String token) {
        return messageService.getAllMessagesOfAConversation(conversationId, token);
    }
}
