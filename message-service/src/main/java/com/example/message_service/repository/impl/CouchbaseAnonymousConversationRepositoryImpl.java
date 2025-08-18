package com.example.message_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.example.message_service.entity.AnonymousConversationDocument;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseAnonymousConversationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CouchbaseAnonymousConversationRepositoryImpl implements ICouchbaseAnonymousConversationRepository {

    private final Collection anonymousConversationsCollection;
    private final ProducerLog producerLog;

    private static final String SCOPE = "messaging";
    private static final String COLLECTION = "anonymous_conversations";

    public CouchbaseAnonymousConversationRepositoryImpl(Cluster cluster,
                                                        Bucket messageBucket,
                                                        ProducerLog producerLog) {
        this.anonymousConversationsCollection = messageBucket.scope(SCOPE).collection(COLLECTION);
        this.producerLog = producerLog;
    }

    @Override
    public AnonymousConversationDocument save(AnonymousConversationDocument conversation) {
        try {
            JsonObject json = conversation.toJsonObject();
            anonymousConversationsCollection.upsert(conversation.getIdConversation(), json);
            return conversation;
        } catch (Exception e) {
            String msg = String.format("Failed to save anonymous conversation %s to %s.%s",
                    conversation.getIdConversation(), SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "ERROR", "[COUCHBASE] " + msg + ": " + e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public Optional<AnonymousConversationDocument> findById(String id) {
        try {
            JsonObject json = anonymousConversationsCollection.get(id).contentAsObject();
            return Optional.of(AnonymousConversationDocument.fromJsonObject(json));
        } catch (Exception e) {
            String msg = String.format("Failed to fetch anonymous conversation %s from %s.%s", id, SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "WARN", "[COUCHBASE] " + msg + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}

