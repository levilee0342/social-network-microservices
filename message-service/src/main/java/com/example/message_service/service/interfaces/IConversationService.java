package com.example.message_service.service.interfaces;

import com.example.message_service.entity.AnonymousConversationDocument;
import com.example.message_service.entity.ConversationDocument;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IConversationService {
    ConversationDocument createConversation(ConversationDocument conversation, HttpServletRequest request);
    AnonymousConversationDocument createAnonymousConversation(List<String> participants);
    List<ConversationDocument> getAllConversationsOfUser(HttpServletRequest request);
}
