package com.example.user_service.repository;

import com.example.user_service.entity.UserPresence;

public interface IRedisUserPresenceRepository {
    UserPresence findByUserId(String userId);
}
