package com.example.presence_service.repository;

import com.example.presence_service.entity.UserPresence;

import java.time.Duration;

public interface IRedisUserPresenceRepository {
    void save(UserPresence presence);
    void saveWithTTL(UserPresence presence, Duration ttl);
    UserPresence findByUserId(String userId);
    void delete(String userId);
}
