package com.example.message_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseMessageRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CouchbaseMessageRepositoryImpl implements ICouchbaseMessageRepository {

    private final Cluster cluster;
    private final Bucket messageBucket;
    private final ProducerLog producerLog;

    private static final String SCOPE = "messaging";
    private static final String COLLECTION = "messages";

    public CouchbaseMessageRepositoryImpl(Cluster cluster, Bucket messageBucket, ProducerLog producerLog) {
        this.cluster = cluster;
        this.messageBucket = messageBucket;
        this.producerLog = producerLog;
    }

    @Override
    public MessageDocument save(MessageDocument message) {
        try {
            messageBucket
                    .scope(SCOPE)
                    .collection(COLLECTION)
                    .upsert(message.getIdMessage(), message.toJsonObject());
            producerLog.sendLog(
                    "message-service",
                    "INFO",
                    String.format("[COUCHBASE] Saved message %s to %s.%s",
                            message.getIdMessage(), SCOPE, COLLECTION)
            );
            return message;
        } catch (Exception e) {
            String msg = String.format("Failed to save message %s to %s.%s",
                    message.getIdMessage(), SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "ERROR", "[COUCHBASE] " + msg + ": " + e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    public Optional<MessageDocument> findById(String id) {
        try {
            JsonObject json = messageBucket
                    .scope(SCOPE)
                    .collection(COLLECTION)
                    .get(id)
                    .contentAsObject();
            MessageDocument message = MessageDocument.fromJsonObject(json);
            producerLog.sendLog(
                    "message-service",
                    "INFO",
                    String.format("[COUCHBASE] Fetched message %s from %s.%s", id, SCOPE, COLLECTION)
            );
            return Optional.of(message);
        } catch (Exception e) {
            String msg = String.format("Failed to fetch message %s from %s.%s", id, SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "ERROR", "[COUCHBASE] " + msg + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<MessageDocument> findByConversationId(String conversationId) {
        try {
            String query = String.format(
                    "SELECT m.* FROM `%s`.`%s`.`%s` m WHERE m.conversationId = $conversationId",
                    messageBucket.name(), SCOPE, COLLECTION
            );

            QueryResult result = cluster.query(
                    query,
                    QueryOptions.queryOptions().parameters(JsonObject.create().put("conversationId", conversationId))
            );

            List<MessageDocument> messages = new ArrayList<>();
            for (JsonObject row : result.rowsAsObject()) {
                producerLog.sendLog(
                        "message-service",
                        "WARN",
                        "[Check mess] mess: " + row);
                messages.add(MessageDocument.fromJsonObject(row));
            }
            producerLog.sendLog(
                    "message-service",
                    "INFO",
                    String.format("[COUCHBASE] Found %s messages for conversation %s from %s.%s",
                            messages, conversationId, SCOPE, COLLECTION)
            );
            return messages;
        } catch (Exception e) {
            String msg = String.format("Failed to fetch messages for conversation %s from %s.%s",
                    conversationId, SCOPE, COLLECTION);
            producerLog.sendLog("message-service", "ERROR", "[COUCHBASE] " + msg + ": " + e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }
}
