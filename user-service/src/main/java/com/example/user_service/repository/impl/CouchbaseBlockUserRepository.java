package com.example.user_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.kv.MutationResult;
import com.example.user_service.entity.BlockUser;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseBlockUserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class CouchbaseBlockUserRepository implements ICouchbaseBlockUserRepository {
    private final Collection blockUserCollection;
    private final ProducerLog logProducer;

    public CouchbaseBlockUserRepository(@Qualifier("friendBucket") Bucket friendBucket,
                                        ProducerLog logProducer) {
        this.blockUserCollection = friendBucket.scope("block").collection("block_user");
        this.logProducer = logProducer;
    }

    private String buildDocId(String blockerId, String blockedId) {
        return "block::" + blockerId + "::" + blockedId;
    }

    @Override
    public void blockUser(String blockerId, String blockedId) {
        try {
            String id = buildDocId(blockerId, blockedId);
            BlockUser blockUser = new BlockUser();
            blockUser.setId(id);
            blockUser.setBlockerId(blockerId);
            blockUser.setBlockedId(blockedId);
            blockUser.setBlockedAt(new Date().getTime());
            MutationResult result = blockUserCollection.upsert(id, blockUser);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[BlockUser] Successfully blocked user " + blockerId + " -> " + blockedId + " at " + blockUser.getBlockedAt() + ". MutationResult: " + result);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[BlockUser] Failed to block user " + blockerId + " -> " + blockedId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void unblockUser(String blockerId, String blockedId) {
        try {
            String id = buildDocId(blockerId, blockedId);
            blockUserCollection.remove(id);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[BlockUser] Successfully unblocked user " + blockerId + " -> " + blockedId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[BlockUser] Failed to unblock user " + blockerId + " -> " + blockedId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isBlocked(String user1, String user2) {
        try {
            String id1 = buildDocId(user1, user2);
            String id2 = buildDocId(user2, user1);
            try {
                GetResult r1 = blockUserCollection.get(id1);
                if (r1 != null) {
                    logProducer.sendLog(
                            "user-service",
                            "WARN", "[BlockUser] " + user1 + " has blocked " + user2);
                    return true;
                }
            } catch (Exception e) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[BlockUser] " + user1 + " has not blocked " + user2 + " or error occurred: " + e.getMessage());
            }

            try {
                GetResult r2 = blockUserCollection.get(id2);
                if (r2 != null) {
                    logProducer.sendLog(
                            "user-service",
                            "WARN",
                            "[BlockUser] " + user2 + " has blocked " + user1);
                    return true;
                }
            } catch (Exception e) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[BlockUser] " + user2 + " has not blocked " + user1 + " or error occurred: " + e.getMessage());
            }
            return false;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[BlockUser] Failed to check block status between " + user1 + " and " + user2 + ". Reason: " + e.getMessage());
            throw e;
        }
    }
}