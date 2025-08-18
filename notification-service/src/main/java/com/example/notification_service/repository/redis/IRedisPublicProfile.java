package com.example.notification_service.repository.redis;

import com.example.notification_service.dto.response.PublicProfileResponse;

public interface IRedisPublicProfile {
    PublicProfileResponse getPublicProfileByUserId(String userId);
}

