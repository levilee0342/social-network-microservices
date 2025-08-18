package com.example.message_service.repository;

import com.example.message_service.entity.MessageDocument;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICouchbaseMessageRepository {
    MessageDocument save(MessageDocument message);
    Optional<MessageDocument> findById(String id);
    List<MessageDocument> findByConversationId(String conversationId);
}