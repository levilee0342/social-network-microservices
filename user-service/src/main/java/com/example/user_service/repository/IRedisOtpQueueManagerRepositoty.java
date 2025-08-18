package com.example.user_service.repository;

public interface IRedisOtpQueueManagerRepositoty {
    void initializeOtpQueue();
    void checkAndRefillOtpQueue();
}
