package com.example.message_service.repository;

import com.example.message_service.entity.ConversationDocument;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ICouchbaseConversationRepository  {
    ConversationDocument save(ConversationDocument conversation);
    Optional<ConversationDocument> findById(String id);
    List<String> getParticipants(String conversationId);
    List<ConversationDocument> findAllByParticipantsContains(String userId);

}