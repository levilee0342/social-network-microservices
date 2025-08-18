package com.example.notification_service.repository.redis.impl;

import com.example.notification_service.dto.response.PublicProfileResponse;
import com.example.notification_service.kafka.producer.ProducerLog;
import com.example.notification_service.repository.redis.IRedisPublicProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublicProfileImpl implements IRedisPublicProfile {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PUBLIC_PROFILE_CACHE_PREFIX = "public_profile:";
    private final ProducerLog producerLog;

    public RedisPublicProfileImpl(RedisTemplate<String, Object> redisTemplate,
                                  ProducerLog producerLog) {
        this.redisTemplate = redisTemplate;
        this.producerLog = producerLog;
    }

    @Override
    public PublicProfileResponse getPublicProfileByUserId(String userId) {
        try {
            Object raw = redisTemplate.opsForValue().get(PUBLIC_PROFILE_CACHE_PREFIX + userId);
            if (raw == null) {
                producerLog.sendLog(
                        "post-service",
                        "WARN",
                        "[PublicProfileCache] Cache miss for: " + PUBLIC_PROFILE_CACHE_PREFIX + userId);
                return null;
            }
            PublicProfileResponse cachedPublicProfile;
            try {
                if (raw instanceof PublicProfileResponse) {
                    cachedPublicProfile = (PublicProfileResponse) raw;
                } else {
                    cachedPublicProfile = new ObjectMapper().convertValue(raw, PublicProfileResponse.class);
                }
                producerLog.sendLog(
                        "post-service",
                        "DEBUG",
                        "[PublicProfileCache] Successfully cache hit for: " + PUBLIC_PROFILE_CACHE_PREFIX + userId);
                return cachedPublicProfile;
            } catch (Exception e) {
                producerLog.sendLog(
                        "post-service",
                        "ERROR",
                        "[PublicProfileCache] Error converting cached public profile for userId: " + userId + ". Reason: " + e.getMessage());
                return null;
            }
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[PublicProfileCache] Failed to retrieve cached public profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }
}
