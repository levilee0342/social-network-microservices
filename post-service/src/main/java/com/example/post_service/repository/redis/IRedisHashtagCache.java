package com.example.post_service.repository.redis;

public interface IRedisHashtagCache {
    void updateHashtagCache(String rawName, boolean increaseScore);
}
