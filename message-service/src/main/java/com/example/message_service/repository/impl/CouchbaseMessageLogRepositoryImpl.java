package com.example.message_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseMessageLogRepository;
import org.springframework.stereotype.Service;

@Service
public class CouchbaseMessageLogRepositoryImpl implements ICouchbaseMessageLogRepository {

    private final Bucket messageBucket;
    private final ProducerLog producerLog;

    private static final String SCOPE = "messaging";
    private static final String LOG_COLLECTION = "message_logs";

    public CouchbaseMessageLogRepositoryImpl(
            Bucket messageBucket,
            ProducerLog producerLog
    ) {
        this.messageBucket = messageBucket;
        this.producerLog = producerLog;
    }

    @Override
    public void saveMessageLog(MessageDocument message) {
        try {
            messageBucket
                    .scope(SCOPE)
                    .collection(LOG_COLLECTION)
                    .upsert(message.getIdMessage(), message.toJsonObject());

            producerLog.sendLog(
                    "message-service",
                    "INFO",
                    String.format("[COUCHBASE] Successfully saved message log %s to %s.%s",
                            message.getIdMessage(), SCOPE, LOG_COLLECTION)
            );
        } catch (Exception e) {
            String msg = String.format("Failed to save message log %s to %s.%s",
                    message.getIdMessage(), SCOPE, LOG_COLLECTION);
            producerLog.sendLog("message-service", "ERROR", "[COUCHBASE] " + msg + ": " + e.getMessage());
            throw new RuntimeException(msg, e);
        }
    }
}
