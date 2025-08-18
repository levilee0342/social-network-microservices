package com.example.presence_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.kv.UpsertOptions;
import com.example.presence_service.entity.UserPresence;
import com.example.presence_service.error.AuthErrorCode;
import com.example.presence_service.exception.AppException;
import com.example.presence_service.kafka.producer.LogProducer;
import com.example.presence_service.repository.ICouchbaseUserPresenceRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class CouchbaseUserPresenceRepositoryImpl implements ICouchbaseUserPresenceRepository {

    private final Collection presenceCollection;
    private final LogProducer logProducer;

    public CouchbaseUserPresenceRepositoryImpl(
            @Qualifier("presenceBucket") Bucket presenceBucket,
            LogProducer logProducer) {
        this.presenceCollection = presenceBucket.scope("status").collection("user_presence");
        this.logProducer = logProducer;
    }

    @Override
    public void save(UserPresence presence) {
        try {
            String docId = "presence::" + presence.getUserId();
            presenceCollection.upsert(docId, presence, UpsertOptions.upsertOptions().expiry(Duration.ofDays(30)));
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "Saved presence to Couchbase for user: " + presence.getUserId());
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to save presence to Couchbase for user: " + presence.getUserId() + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SAVE_COUCHBASE);
        }
    }

    @Override
    public UserPresence findByUserId(String userId) {
        try {
            var result = presenceCollection.get("presence::" + userId);
            UserPresence presence = result.contentAs(UserPresence.class);
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "Retrieved presence from Couchbase for user: " + userId);
            return presence;
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "WARN",
                    "No presence found in Couchbase for user: " + userId);
            return null;
        }
    }
}