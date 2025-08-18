package com.example.post_service.repository.redis;

import com.example.post_service.dto.response.PublicProfileResponse;

public interface IRedisPublicProfile {
    PublicProfileResponse getPublicProfileByUserId(String userId);
}
