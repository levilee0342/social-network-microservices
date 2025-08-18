package com.example.user_service.repository.impl;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.example.user_service.entity.FriendRequest;
import com.example.user_service.entity.Friendship;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.error.NotExistedErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseFriendRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CouchbaseFriendRepository implements ICouchbaseFriendRepository {
    private final Cluster couchbaseCluster;
    private final Collection friendRequestCollection;
    private final Collection friendshipCollection;
    private final ProducerLog logProducer;

    public CouchbaseFriendRepository(@Qualifier("friendCouchbaseCluster") Cluster couchbaseCluster,
                                     @Qualifier("friendBucket") Bucket friendBucket,
                                     ProducerLog logProducer) {
        this.couchbaseCluster = couchbaseCluster;
        this.friendRequestCollection = friendBucket.scope("relationships").collection("friend_requests");
        this.friendshipCollection = friendBucket.scope("relationships").collection("friendships");
        this.logProducer = logProducer;
    }

    @Override
    public void saveFriendRequest(FriendRequest friendRequest) {
        try {
            if (friendRequest.getRequestId() == null) {
                friendRequest.setRequestId(UUID.randomUUID().toString());
                logProducer.sendLog(
                        "user-service",
                        "INFO",
                        "[Friend] Generated new requestId: " + friendRequest.getRequestId() + " for friend request");
            }
            String docId = "friendRequest::" + friendRequest.getRequestId();
            friendRequestCollection.upsert(docId, friendRequest);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Successfully saved friend request: " + docId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to save friend request with requestId: " + (friendRequest != null ? friendRequest.getRequestId() : "null") + ". Reason: " + e.getMessage());
        }
    }

    @Override
    public FriendRequest getFriendRequest(String requestId) {
        try {
            String docId = "friendRequest::" + requestId;
            FriendRequest friendRequest = friendRequestCollection.get(docId).contentAs(FriendRequest.class);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Successfully retrieved friend request: " + docId);
            return friendRequest;
        } catch (DocumentNotFoundException e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Friend request not found for requestId: " + requestId);
            throw new AppException(NotExistedErrorCode.FRIEND_REQUEST_NOT_FOUND);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to retrieve friend request with requestId: " + requestId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_FRIEND_REQUEST);
        }
    }

    @Override
    public void updateFriendRequest(FriendRequest friendRequest) {
        try {
            String docId = "friendRequest::" + friendRequest.getRequestId();
            friendRequestCollection.upsert(docId, friendRequest);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Successfully updated friend request: " + docId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to update friend request with requestId: " + (friendRequest != null ? friendRequest.getRequestId() : "null") + ". Reason: " + e.getMessage());
        }
    }

    @Override
    public boolean hasPendingRequest(String senderId, String receiverId) {
        try {
            String query = "SELECT COUNT(*) as count FROM `friend_bucket`.`relationships`.`friend_requests` " +
                    "WHERE senderId = $1 AND receiverId = $2 AND status = 'PENDING'";
            QueryResult result = couchbaseCluster.query(
                    query,
                    QueryOptions.queryOptions().parameters(JsonArray.from(senderId, receiverId))
            );
            long count = result.rowsAsObject().get(0).getLong("count");
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Pending request check result: " + (count > 0) + " for senderId: " + senderId + " to receiverId: " + receiverId);
            return count > 0;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to check pending friend request from senderId: " + senderId + " to receiverId: " + receiverId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_CHECK_PENDING_FRIEND);
        }
    }

    @Override
    public List<FriendRequest> getPendingFriendRequests(String userId) {
        try {
            String query = "SELECT friend_requests.* FROM `friend_bucket`.`relationships`.`friend_requests` " +
                    "WHERE receiverId = $userId AND status = 'PENDING'";
            JsonObject params = JsonObject.create().put("userId", userId);
            QueryResult result = couchbaseCluster.query(
                    query,
                    QueryOptions.queryOptions().parameters(params)
            );
            List<FriendRequest> requests = result.rowsAs(FriendRequest.class);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Successfully retrieved " + requests.size() + " pending friend requests for userId: " + userId);
            return requests;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to retrieve pending friend requests for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_PENDING_FRIEND);
        }
    }

    @Override
    public void saveFriendship(Friendship friendship) {
        try {
            if (friendship.getFriendshipId() == null) {
                friendship.setFriendshipId(UUID.randomUUID().toString());
            }
            String docId = "friendship::" + friendship.getFriendshipId();
            friendshipCollection.upsert(docId, friendship);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Successfully saved friendship: " + docId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to save friendship with friendshipId: " + (friendship != null ? friendship.getFriendshipId() : "null") + ". Reason: " + e.getMessage());
        }
    }

    @Override
    public List<Friendship> getFriendships(String userId) {
        try {
            String query = "SELECT friendships.* FROM `friend_bucket`.`relationships`.`friendships` " +
                    "WHERE userId1 = $userId OR userId2 = $userId";
            JsonObject params = JsonObject.create().put("userId", userId);
            QueryResult result = couchbaseCluster.query(
                    query,
                    QueryOptions.queryOptions().parameters(params)
            );
            List<Friendship> friendships = result.rowsAs(Friendship.class);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Successfully retrieved " + friendships.size() + " friendships for userId: " + userId);
            return friendships;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to retrieve friendships for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_FRIENDSHIP);
        }
    }

    @Override
    public boolean isFriend(String userId1, String userId2) {
        try {
            String query = "SELECT COUNT(*) as count FROM `friend_bucket`.`relationships`.`friendships` " +
                    "WHERE (userId1 = $1 AND userId2 = $2) OR (userId1 = $2 AND userId2 = $1)";
            QueryResult result = couchbaseCluster.query(
                    query,
                    QueryOptions.queryOptions().parameters(JsonArray.from(userId1, userId2))
            );
            long count = result.rowsAsObject().get(0).getLong("count");
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Friendship check result: " + (count > 0) + " for userId1: " + userId1 + " and userId2: " + userId2);
            return count > 0;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to check friendship between userId1: " + userId1 + " and userId2: " + userId2 + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_CHECK_FRIEND);
        }
    }

    @Override
    public boolean removeFriendship(String userId1, String userId2) {
        try {
            String query = "SELECT META().id FROM `friend_bucket`.`relationships`.`friendships` " +
                    "WHERE (userId1 = $1 AND userId2 = $2) OR (userId1 = $2 AND userId2 = $1)";
            QueryResult result = couchbaseCluster.query(
                    query,
                    QueryOptions.queryOptions().parameters(JsonArray.from(userId1, userId2))
            );
            if (result.rowsAsObject().isEmpty()) {
                logProducer.sendLog(
                        "user-service",
                        "INFO",
                        "[Friend] No friendship found between userId1: " + userId1 + " and userId2: " + userId2);
                return false;
            }
            String friendshipId = result.rowsAsObject().get(0).getString("id");
            friendshipCollection.remove(friendshipId);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Successfully removed friendship: " + friendshipId + " between userId1: " + userId1 + " and userId2: " + userId2);
            return true;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to remove friendship between userId1: " + userId1 + " and userId2: " + userId2 + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_REMOVE_FRIENDSHIP);
        }
    }
}