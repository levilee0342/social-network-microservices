package com.example.user_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseTokenRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class CouchbaseTokenRepository implements ICouchbaseTokenRepository {
    private final Collection tokenCollection;
    private final ProducerLog logProducer;

    public CouchbaseTokenRepository(@Qualifier("userBucket") Bucket userBucket,
                                    ProducerLog logProducer) {
        this.tokenCollection = userBucket.scope("identity").collection("token");
        this.logProducer = logProducer;
    }

    @Override
    public void saveTokens(String tokenKey, Map<String, String> tokens) {
        try {
            tokenCollection.upsert(tokenKey, tokens);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Token] Failed to save tokens for tokenKey: " + tokenKey + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SAVE_TOKEN);
        }
    }

    @Override
    public Map<String, String> getTokens(String tokenKey) {
        try {
            Map<String, String> tokens = tokenCollection.get(tokenKey).contentAs(Map.class);
            return tokens;
        } catch (com.couchbase.client.core.error.DocumentNotFoundException e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Token] Tokens not found for tokenKey: " + tokenKey);
            throw new IllegalArgumentException("Tokens not found for key: " + tokenKey, e);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Token] Failed to retrieve tokens for tokenKey: " + tokenKey + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_TOKEN);
        }
    }

    @Override
    public void removeTokens(String tokenKey) {
        try {
            tokenCollection.remove(tokenKey);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Token] Successfully removed tokens for tokenKey: " + tokenKey);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Token] Failed to remove tokens for tokenKey: " + tokenKey + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_REMOVE_TOKEN);
        }
    }
}