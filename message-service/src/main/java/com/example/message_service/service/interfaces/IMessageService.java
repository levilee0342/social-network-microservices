package com.example.message_service.service.interfaces;

import com.example.message_service.entity.MessageDocument;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IMessageService {
    MessageDocument sendMessage(MessageDocument message, String token,String userId);
    void markMessageAsRead(String messageId, String userId);
    public void deleteMessageForUser(String messageId, String userId);
    List<MessageDocument> getAllMessagesOfAConversation(String conversationId, String token);

}
