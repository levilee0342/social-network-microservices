package com.example.message_service.repository;

import com.example.message_service.entity.AnonymousConversationDocument;

import java.util.Optional;

public interface ICouchbaseAnonymousConversationRepository {
    AnonymousConversationDocument save(AnonymousConversationDocument conversation);
    Optional<AnonymousConversationDocument> findById(String id);
}
