package com.example.user_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.couchbase.client.java.query.QueryResult;
import com.example.user_service.entity.UserDailyActivity;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseUserDailyActivityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.couchbase.client.java.query.QueryOptions.queryOptions;

@Repository
public class CouchbaseUserDailyActivityRepository implements ICouchbaseUserDailyActivityRepository {
    private final Collection collection;
    private final Cluster cluster;
    private final ObjectMapper objectMapper;
    private final ProducerLog logProducer;

    public CouchbaseUserDailyActivityRepository(@Qualifier("userBucket") Bucket bucket,
                                                @Qualifier("userCouchbaseCluster") Cluster cluster,
                                                ObjectMapper objectMapper,
                                                ProducerLog logProducer) {
        this.collection = bucket.scope("user_activity").collection("user_daily_activity");
        this.cluster = cluster;
        this.objectMapper = objectMapper;
        this.logProducer = logProducer;
    }

    @Override
    public Optional<UserDailyActivity> findByUserIdAndDate(String userId, Long date) {
        try {
            String docId = userId + "_" + date;
            GetResult result = collection.get(docId);
            UserDailyActivity activity = objectMapper.readValue(result.contentAs(String.class), UserDailyActivity.class);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[UserActivity] Successfully found daily activity for userId: " + userId + " and date: " + date);
            return Optional.of(activity);
        } catch (com.couchbase.client.core.error.DocumentNotFoundException e) {
            logProducer.sendLog(
                    "user-service",
                    "WARN",
                    "[UserActivity] No daily activity found for userId: " + userId + " and date: " + date);
            return Optional.empty();
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[UserActivity] Failed to find daily activity for userId: " + userId + " and date: " + date + ". Reason: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<UserDailyActivity> findByUserIdOrderByDateDesc(String userId) {
        try {
            String query = "SELECT d.* FROM `user_bucket`.`user_activity`.`user_daily_activity` d " +
                    "WHERE d.userId = $userId ORDER BY d.date DESC";
            QueryResult result = cluster.query(query,
                    queryOptions().parameters(JsonObject.create().put("userId", userId)));
            List<UserDailyActivity> activities = result.rowsAs(UserDailyActivity.class);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[UserActivity] Successfully retrieved "
                            + activities.size() + " daily activities for userId: " + userId);
            return activities;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[UserActivity] Failed to find daily activities for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_FIND_DAILY);
        }
    }

    @Override
    public UserDailyActivity save(UserDailyActivity activity) {
        try {
            String docId = activity.getUserId() + "_" + activity.getDate();
            collection.upsert(docId, activity);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[UserActivity] Successfully saved daily activity for userId: "
                            + activity.getUserId() + " and date: " + activity.getDate());
            return activity;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[UserActivity] Failed to save daily activity for userId: " +
                    (activity != null ? activity.getUserId() : "null") + " and date: " + (activity != null ? activity.getDate() : "null") + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SAVE_DAILY);
        }
    }
}