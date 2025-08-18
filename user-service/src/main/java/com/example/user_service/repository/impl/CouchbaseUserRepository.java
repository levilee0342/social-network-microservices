package com.example.user_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Collection;
import com.example.user_service.entity.User;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.error.NotExistedErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseUserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class CouchbaseUserRepository implements ICouchbaseUserRepository {
    private final Collection userCollection;
    private final ProducerLog logProducer;

    public CouchbaseUserRepository(@Qualifier("userBucket") Bucket userBucket,
                                   ProducerLog logProducer) {
        this.userCollection = userBucket.scope("identity").collection("users");
        this.logProducer = logProducer;
    }

    @Override
    public boolean exists(String userKey) {
        try {
            boolean exists = userCollection.exists(userKey).exists();
            logProducer.sendLog(
                    "user-service",
                    "INFO",
                    "[User] User existence check result: " + exists + " for userKey: " + userKey);
            return exists;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[User] Failed to check existence of user with userKey: " + userKey + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_CHECK_USER);
        }
    }

    @Override
    public void saveUser(String userKey, User user) {
        try {
            userCollection.upsert(userKey, user);
            logProducer.sendLog(
                    "user-service",
                    "INFO",
                    "[User] Successfully saved user with userKey: " + userKey);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[User] Failed to save user with userKey: " + userKey + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SAVE_USER);
        }
    }

    @Override
    public User getUser(String userKey) {
        try {
            User user = userCollection.get(userKey).contentAs(User.class);
            logProducer.sendLog(
                    "user-service",
                    "INFO",
                    "[User] Successfully retrieved user with userKey: " + userKey);
            return user;
        } catch (com.couchbase.client.core.error.DocumentNotFoundException e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[User] User not found for userKey: " + userKey);
            throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[User] Failed to retrieve user with userKey: " + userKey + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_USER);
        }
    }
}