package com.example.message_service.repository;

import com.example.message_service.entity.UserPresence;

public interface IRedisUserPresenceRedisRepository {
    UserPresence findByUserId(String userId);
    void save(UserPresence presence);
}
