package com.example.message_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.example.message_service.entity.BlockMessageDocument;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseBlockMessageRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CouchbaseBlockMessageRepositoryImpl implements ICouchbaseBlockMessageRepository {

    private final Collection blockMessageCollection;
    private final ProducerLog producerLog;
    private static final String SCOPE = "block";
    private static final String COLLECTION = "block_message";

    public CouchbaseBlockMessageRepositoryImpl(Cluster couchbaseCluster,
                                               @Qualifier("messageBucket") Bucket messageBucket,
                                               ProducerLog producerLog) {
        this.blockMessageCollection = messageBucket.scope(SCOPE).collection(COLLECTION);
        this.producerLog = producerLog;
    }

    private String buildDocId(String blockerId, String blockedId) {
        return "blockMessage::" + blockerId + "::" + blockedId;
    }

    @Override
    public void blockMessage(String blockerId, String blockedId) {
        String id = buildDocId(blockerId, blockedId);
        try {
            BlockMessageDocument blockMessage = new BlockMessageDocument();
            blockMessage.setId(id);
            blockMessage.setBlockerId(blockerId);
            blockMessage.setBlockedId(blockedId);
            blockMessage.setBlockedAt(new Date().getTime());

            MutationResult result = blockMessageCollection.upsert(id, blockMessage);

            producerLog.sendLog(
                    "message-service",
                    "INFO",
                    String.format("[BlockMessage] %s blocked messages from %s at %d. Result: %s",
                            blockerId, blockedId, blockMessage.getBlockedAt(), result));
        } catch (Exception e) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    String.format("[BlockMessage] Failed to block messages from %s by %s: %s",
                            blockedId, blockerId, e.getMessage()));
        }
    }

    @Override
    public void unblockMessage(String blockerId, String blockedId) {
        String id = buildDocId(blockerId, blockedId);
        try {
            blockMessageCollection.remove(id);
            producerLog.sendLog(
                    "message-service",
                    "INFO",
                    String.format("[UnblockMessage] %s unblocked messages from %s",
                            blockerId, blockedId));
        } catch (Exception e) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    String.format("[UnblockMessage] Failed to unblock messages from %s by %s: %s",
                            blockedId, blockerId, e.getMessage()));
        }
    }

    @Override
    public boolean isBlocked(String senderId, String receiverId) {
        // Kiểm tra nếu receiver đã chặn sender
        String id = buildDocId(receiverId, senderId);

        try {
            GetResult result = blockMessageCollection.get(id);
            if (result != null) {
                producerLog.sendLog(
                        "message-service",
                        "INFO",
                        String.format("[CheckMessageBlock] %s has blocked messages from %s", receiverId, senderId));
                return true;
            }
        } catch (Exception e) {
            producerLog.sendLog(
                    "message-service",
                    "DEBUG",
                    String.format("[CheckMessageBlock] No block found or error between %s and %s: %s",
                            receiverId, senderId, e.getMessage()));
        }

        producerLog.sendLog(
                "message-service",
                "INFO",
                String.format("[CheckMessageBlock] No message block found between %s and %s",
                        receiverId, senderId));
        return false;
    }
}
