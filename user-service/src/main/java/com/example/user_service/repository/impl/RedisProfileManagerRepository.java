package com.example.user_service.repository.impl;

import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.IRedisProfileManagerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisProfileManagerRepository implements IRedisProfileManagerRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProducerLog logProducer;
    private static final String PROFILE_CACHE_PREFIX = "profile:";
    private static final String PUBLIC_PROFILE_CACHE_PREFIX = "public_profile:";
    private static final long CACHE_TTL_MINUTES = 60;

    public RedisProfileManagerRepository(RedisTemplate<String, Object> redisTemplate, ProducerLog logProducer) {
        this.redisTemplate = redisTemplate;
        this.logProducer = logProducer;
    }

    @Override
    public void cacheProfile(String userId, ProfileResponse response) {
        logProducer.sendLog("user-service", "DEBUG", "[ProfileCache] Saving profile to cache for userId: " + userId);
        try {
            Map<String, Object> cacheMap = new HashMap<>();
            cacheMap.put("userId", response.getUserId());
            cacheMap.put("fullName", response.getFullName());
            cacheMap.put("avatarUrl", response.getAvatarUrl());
            cacheMap.put("phone", response.getPhone());
            cacheMap.put("address", response.getAddress());
            cacheMap.put("isPublic", response.getIsPublic());
            cacheMap.put("gender", response.getGender());
            cacheMap.put("profileId", response.getProfileId());
            cacheMap.put("createdAt", response.getCreatedAt());
            cacheMap.put("updatedAt", response.getUpdatedAt());
            cacheMap.put("dateOfBirth", response.getDateOfBirth() != null
                    ? response.getDateOfBirth().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                    : null);
            redisTemplate.opsForValue().set(PROFILE_CACHE_PREFIX + userId, cacheMap, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            logProducer.sendLog("user-service", "INFO", "[ProfileCache] Successfully saved profile to cache: " + PROFILE_CACHE_PREFIX + userId);
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[ProfileCache] Failed to save profile to cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public ProfileResponse getCachedProfile(String userId) {
        try {
            Object raw = redisTemplate.opsForValue().get(PROFILE_CACHE_PREFIX + userId);
            if (raw == null) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileCache] Cache miss for: " + PROFILE_CACHE_PREFIX + userId);
                return null;
            }

            if (!(raw instanceof Map<?, ?> rawMap)) {
                logProducer.sendLog("user-service", "ERROR", "[ProfileCache] Unexpected cached data format.");
                return null;
            }

            ProfileResponse profile = new ProfileResponse();
            profile.setUserId((String) rawMap.get("userId"));
            profile.setFullName((String) rawMap.get("fullName"));
            profile.setAvatarUrl((String) rawMap.get("avatarUrl"));
            profile.setPhone((String) rawMap.get("phone"));
            profile.setAddress((String) rawMap.get("address"));
            profile.setIsPublic(Boolean.TRUE.equals(rawMap.get("isPublic")));
            profile.setGender(Boolean.TRUE.equals(rawMap.get("gender")));
            profile.setProfileId((String) rawMap.get("profileId"));
            profile.setCreatedAt((Date) rawMap.get("createdAt"));
            profile.setUpdatedAt((Date) rawMap.get("updatedAt"));

            Long dateOfBirthMillis = rawMap.get("dateOfBirth") instanceof Number dob
                    ? dob.longValue() : null;
            profile.setDateOfBirth(dateOfBirthMillis != null
                    ? Instant.ofEpochMilli(dateOfBirthMillis).atZone(ZoneId.of("UTC")).toLocalDate()
                    : null);

            logProducer.sendLog("user-service", "INFO", "[ProfileCache] Cache hit for: " + PROFILE_CACHE_PREFIX + userId);
            return profile;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[ProfileCache] Failed to retrieve cached profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }


    @Override
    public void cachePublicProfile(String userId, PublicProfileResponse response) {
        try {
            redisTemplate.opsForValue().set(PUBLIC_PROFILE_CACHE_PREFIX + userId, response);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[ProfileCache] Successfully saved public profile to cache: " + PUBLIC_PROFILE_CACHE_PREFIX + userId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[ProfileCache] Failed to save public profile to cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public PublicProfileResponse getCachedPublicProfile(String userId) {
        logProducer.sendLog("user-service", "DEBUG", "[ProfileCache] Retrieving cached public profile for userId: " + userId);
        try {
            Object raw = redisTemplate.opsForValue().get(PUBLIC_PROFILE_CACHE_PREFIX + userId);
            if (raw == null) {
                logProducer.sendLog("user-service", "DEBUG", "[ProfileCache] Cache miss for: " + PUBLIC_PROFILE_CACHE_PREFIX + userId);
                return null;
            }

            PublicProfileResponse cachedPublicProfile;
            try {
                if (raw instanceof PublicProfileResponse) {
                    cachedPublicProfile = (PublicProfileResponse) raw;
                } else {
                    cachedPublicProfile = new ObjectMapper().convertValue(raw, PublicProfileResponse.class);
                }
                logProducer.sendLog("user-service", "INFO", "[ProfileCache] Cache hit for: " + PUBLIC_PROFILE_CACHE_PREFIX + userId);
                return cachedPublicProfile;
            } catch (Exception e) {
                logProducer.sendLog("user-service", "ERROR", "[ProfileCache] Error converting cached public profile for userId: " + userId + ". Reason: " + e.getMessage());
                return null;
            }
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[ProfileCache] Failed to retrieve cached public profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteCachedProfile(String userId) {
        logProducer.sendLog("user-service", "DEBUG", "[ProfileCache] Deleting cached profile for userId: " + userId);
        try {
            redisTemplate.delete(PROFILE_CACHE_PREFIX + userId);
            logProducer.sendLog("user-service", "INFO", "[ProfileCache] Successfully deleted cached profile: " + PROFILE_CACHE_PREFIX + userId);
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[ProfileCache] Failed to delete cached profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteCachedPublicProfile(String userId) {
        logProducer.sendLog("user-service", "DEBUG", "[ProfileCache] Deleting cached public profile for userId: " + userId);
        try {
            redisTemplate.delete(PUBLIC_PROFILE_CACHE_PREFIX + userId);
            logProducer.sendLog("user-service", "INFO", "[ProfileCache] Successfully deleted cached public profile: " + PUBLIC_PROFILE_CACHE_PREFIX + userId);
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[ProfileCache] Failed to delete cached public profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }
}