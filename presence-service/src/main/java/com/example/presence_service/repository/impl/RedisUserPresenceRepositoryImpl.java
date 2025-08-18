package com.example.presence_service.repository.impl;

import com.example.presence_service.entity.UserPresence;
import com.example.presence_service.error.AuthErrorCode;
import com.example.presence_service.exception.AppException;
import com.example.presence_service.kafka.producer.LogProducer;
import com.example.presence_service.repository.IRedisUserPresenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RedisUserPresenceRepositoryImpl implements IRedisUserPresenceRepository {

    private static final String PRESENCE_KEY_PREFIX = "presence:user:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(7);
    private final RedisTemplate<String, Object> redisTemplate;
    private final LogProducer logProducer;

    public RedisUserPresenceRepositoryImpl(
            RedisTemplate<String, Object> redisTemplate,
            LogProducer logProducer) {
        this.redisTemplate = redisTemplate;
        this.logProducer = logProducer;
    }

    @Override
    public void save(UserPresence presence) {
        saveWithTTL(presence, DEFAULT_TTL);
    }

    @Override
    public void saveWithTTL(UserPresence presence, Duration ttl) {
        String key = PRESENCE_KEY_PREFIX + presence.getUserId();
        try {
            redisTemplate.opsForValue().set(key, presence, ttl);
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "Saved presence to Redis for user: " + presence.getUserId() + " with TTL: " + ttl.getSeconds() + "s");
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to save presence to Redis for user: " + presence.getUserId() + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SAVE_REDIS);
        }
    }

    @Override
    public UserPresence findByUserId(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                logProducer.sendLog(
                        "presence-service",
                        "WARN",
                        "No presence found in Redis for user: " + userId);
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            UserPresence presence = mapper.convertValue(raw, UserPresence.class);
            logProducer.sendLog(
                    "presence-service",
                    "INFO",
                    "Retrieved presence from Redis for user: " + userId);
            return presence;
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to retrieve presence from Redis for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_RETRIEVE_PRESENCE);
        }
    }

    @Override
    public void delete(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        try {
            redisTemplate.delete(key);
            logProducer.sendLog(
                    "presence-service",
                    "INFO",
                    "Deleted presence from Redis for user: " + userId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to delete presence from Redis for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_DELETE_PRESENCE);
        }
    }
}