package com.example.message_service.service;

import com.example.message_service.entity.ConversationDocument;
import com.example.message_service.service.interfaces.IConversationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    private final IConversationService conversationService;

    public ConversationService(IConversationService conversationService) {
        this.conversationService = conversationService;
    }

    public ConversationDocument createConversation(ConversationDocument conversation, HttpServletRequest request) {
        return conversationService.createConversation(conversation, request);
    }
    public List<ConversationDocument> getAllConversationsOfUser(HttpServletRequest request) {
        return conversationService.getAllConversationsOfUser(request);
    }

}
