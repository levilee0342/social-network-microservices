package com.example.message_service.repository.impl;

import com.example.message_service.entity.UserPresence;
import com.example.message_service.error.AuthErrorCode;
import com.example.message_service.exception.AppException;
import com.example.message_service.repository.IRedisUserPresenceRedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RedisUserPresenceRedisRepositoryImpl implements IRedisUserPresenceRedisRepository {

    private static final String PRESENCE_KEY_PREFIX = "presence:user:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUserPresenceRedisRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public UserPresence findByUserId(String userId) {
        String key = PRESENCE_KEY_PREFIX + userId;
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) return null;

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.convertValue(raw, UserPresence.class);
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.FAILED_RETRIEVE_PRESENCE);
        }
    }

    @Override
    public void save(UserPresence presence) {
        String key = PRESENCE_KEY_PREFIX + presence.getUserId();
        try {
            redisTemplate.opsForValue().set(key, presence, DEFAULT_TTL);
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.FAILED_SAVE_REDIS);
        }
    }
}
