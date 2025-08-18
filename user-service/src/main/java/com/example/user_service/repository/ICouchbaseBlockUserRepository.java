package com.example.user_service.repository;

public interface ICouchbaseBlockUserRepository {
    void blockUser(String blockerId, String blockedId);
    void unblockUser(String blockerId, String blockedId);
    boolean isBlocked(String user1, String user2);
}
