package com.example.message_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.example.message_service.entity.ConversationDocument;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseConversationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.couchbase.client.core.service.ServiceScope.BUCKET;

@Service
public class CouchbaseConversationRepositoryImpl implements ICouchbaseConversationRepository {

    private final Collection conversationsCollection;
    private final ProducerLog producerLog;

    private static final String SCOPE = "messaging";
    private static final String COLLECTION = "conversations";
    private final Cluster couchbaseCluster;
    private final Bucket messageBucket;

    public CouchbaseConversationRepositoryImpl(
            Cluster couchbaseCluster,
            Bucket messageBucket,
            ProducerLog producerLog
    ) {
        this.couchbaseCluster = couchbaseCluster;
        this.messageBucket = messageBucket;
        this.conversationsCollection = messageBucket.scope(SCOPE).collection(COLLECTION);
        this.producerLog = producerLog;
    }

    @Override
    public ConversationDocument save(ConversationDocument conversation) {
        try {
            conversationsCollection.upsert(conversation.getIdConversation(), conversation.toJsonObject());
            return conversation;
        } catch (Exception e) {
            String msg = String.format("Failed to save conversation %s to %s.%s",
                    conversation.getIdConversation(), SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "ERROR", "[COUCHBASE] " + msg + ": " + e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public Optional<ConversationDocument> findById(String id) {
        try {
            JsonObject json = conversationsCollection.get(id).contentAsObject();
            return Optional.of(ConversationDocument.fromJsonObject(json));
        } catch (Exception e) {
            String msg = String.format("Failed to fetch conversation %s from %s.%s", id, SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "WARN", "[COUCHBASE] " + msg + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<String> getParticipants(String conversationId) {
        try {
            JsonObject json = conversationsCollection.get(conversationId).contentAsObject();
            if (json.containsKey("participantIds")) {
                return json.getArray("participantIds")
                        .toList()
                        .stream()
                        .map(Object::toString)
                        .toList();
            }
            return List.of();
        } catch (Exception e) {
            String msg = String.format("Failed to fetch participants of conversation %s from %s.%s",
                    conversationId, SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "WARN", "[COUCHBASE] " + msg + ": " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<ConversationDocument> findAllByParticipantsContains(String userId) {
        List<ConversationDocument> result = new ArrayList<>();
        try {
            String bucketName = messageBucket.name();
            String statement = String.format(
                    "SELECT c.* FROM `%s`.`%s`.`%s` c WHERE ARRAY_CONTAINS(c.participants, $userId)",
                    bucketName, SCOPE, COLLECTION
            );
            QueryResult queryResult = couchbaseCluster.query(
                    statement,
                    QueryOptions.queryOptions().parameters(JsonObject.create().put("userId", userId))
            );

            for (JsonObject row : queryResult.rowsAsObject()) {
                result.add(ConversationDocument.fromJsonObject(row));
            }
            return result;
        } catch (Exception e) {
            String msg = String.format("Failed to query conversations containing user %s from %s.%s", userId, SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "ERROR", "[COUCHBASE] " + msg + ": " + e.getMessage());
            return result;
        }
    }

}
