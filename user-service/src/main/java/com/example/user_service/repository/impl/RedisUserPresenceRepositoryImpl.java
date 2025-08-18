package com.example.user_service.repository.impl;

import com.example.user_service.entity.UserPresence;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.IRedisUserPresenceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisUserPresenceRepositoryImpl implements IRedisUserPresenceRepository {

    private static final String PRESENCE_KEY_PREFIX = "presence:user:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProducerLog logProducer;

    public RedisUserPresenceRepositoryImpl(
            RedisTemplate<String, Object> redisTemplate,
            ProducerLog logProducer) {
        this.redisTemplate = redisTemplate;
        this.logProducer = logProducer;
    }

    @Override
    public UserPresence findByUserId(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "No presence found in Redis for user: " + userId);
                return null;
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            UserPresence presence = mapper.convertValue(raw, UserPresence.class);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "Retrieved presence from Redis for user: " + userId);
            return presence;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "Failed to retrieve presence from Redis for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_PRESENCE);
        }
    }
}
