package com.example.message_service.repository;

import org.springframework.stereotype.Repository;

@Repository
public interface ICouchbaseBlockMessageRepository {
    void blockMessage(String blockerId, String blockedId);
    void unblockMessage(String blockerId, String blockedId);
    boolean isBlocked(String user1, String user2);
}
