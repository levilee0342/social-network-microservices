package com.example.presence_service.repository;

import com.example.presence_service.entity.UserPresence;

public interface ICouchbaseUserPresenceRepository {
    void save(UserPresence presence);
    UserPresence findByUserId(String userId);
}
