package com.example.user_service.repository.impl;

import com.example.user_service.dto.response.PublicProfileDetailsResponse;
import com.example.user_service.dto.response.profile_details.*;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.IRedisProfileDetailManagerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RedisProfileDetailManagerRepository implements IRedisProfileDetailManagerRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ProducerLog logProducer;
    private final ObjectMapper mapper;
    private static final String PROFILE_DETAIL_CACHE_PREFIX = "profile_detail:";
    private static final String PUBLIC_PROFILE_DETAIL_CACHE_PREFIX = "public_profile_detail:";
    private static final long CACHE_TTL_MINUTES = 60;

    public RedisProfileDetailManagerRepository(RedisTemplate<String, Object> redisTemplate,
                                               ProducerLog logProducer,
                                               ObjectMapper mapper) {
        this.redisTemplate = redisTemplate;
        this.logProducer = logProducer;
        this.mapper = mapper;
    }

    @Override
    public void cacheProfileDetail(String userId, PublicProfileDetailsResponse response) {
        logProducer.sendLog("user-service",
                "DEBUG",
                "[ProfileDetailCache] Saving profile detail to cache for userId: " + userId);
        try {
            Map<String, Object> cacheMap = new HashMap<>();
            cacheMap.put("userId", response.getUserId());
            cacheMap.put("profileDetailsId", response.getProfileDetailsId());

            if (response.getEducation() != null) {
                cacheMap.put("education", mapper.convertValue(response.getEducation(), new TypeReference<List<Map<String, Object>>>() {}));
            }

            if (response.getWork() != null) {
                cacheMap.put("work", mapper.convertValue(response.getWork(), new TypeReference<List<Map<String, Object>>>() {}));
            }

            if (response.getLocation() != null) {
                cacheMap.put("location", mapper.convertValue(response.getLocation(), new TypeReference<Map<String, Object>>() {}));
            }

            if (response.getContact() != null) {
                cacheMap.put("contact", mapper.convertValue(response.getContact(), new TypeReference<Map<String, Object>>() {}));
            }

            if (response.getRelationship() != null) {
                cacheMap.put("relationship", mapper.convertValue(response.getRelationship(), new TypeReference<Map<String, Object>>() {}));
            }

            if (response.getFamilyMembers() != null) {
                cacheMap.put("familyMembers", mapper.convertValue(response.getFamilyMembers(), new TypeReference<List<Map<String, Object>>>() {}));
            }

            if (response.getDetail() != null) {
                cacheMap.put("detail", mapper.convertValue(response.getDetail(), new TypeReference<Map<String, Object>>() {}));
            }

            cacheMap.put("createdAt", response.getCreatedAt());
            cacheMap.put("updatedAt", response.getUpdatedAt());

            redisTemplate.opsForValue().set(PROFILE_DETAIL_CACHE_PREFIX + userId, cacheMap, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

            logProducer.sendLog("user-service",
                    "INFO",
                    "[ProfileDetailCache] Successfully saved profile detail to cache: " + PROFILE_DETAIL_CACHE_PREFIX + userId);
        } catch (Exception e) {
            logProducer.sendLog("user-service",
                    "ERROR",
                    "[ProfileDetailCache] Failed to save profile detail to cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public PublicProfileDetailsResponse getCachedProfileDetail(String userId) {
        String cacheKey = PROFILE_DETAIL_CACHE_PREFIX + userId;
        logProducer.sendLog("user-service",
                "DEBUG",
                "[ProfileDetailCache] Fetching profile detail from cache for userId: " + userId);
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                logProducer.sendLog("user-service",
                        "INFO",
                        "[ProfileDetailCache] No cached profile detail found for userId: " + userId);
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> cacheMap = (Map<String, Object>) cached;

            PublicProfileDetailsResponse response = new PublicProfileDetailsResponse();
            response.setUserId((String) cacheMap.get("userId"));
            response.setProfileDetailsId((String) cacheMap.get("profileDetailsId"));

            if (cacheMap.containsKey("education")) {
                List<EducationResponse> education = mapper.convertValue(
                        cacheMap.get("education"), new TypeReference<List<EducationResponse>>() {});
                response.setEducation(education);
            }

            if (cacheMap.containsKey("work")) {
                List<WorkResponse> work = mapper.convertValue(
                        cacheMap.get("work"), new TypeReference<List<WorkResponse>>() {});
                response.setWork(work);
            }

            if (cacheMap.containsKey("location")) {
                LocationResponse location = mapper.convertValue(cacheMap.get("location"), LocationResponse.class);
                response.setLocation(location);
            }

            if (cacheMap.containsKey("contact")) {
                ContactResponse contact = mapper.convertValue(cacheMap.get("contact"), ContactResponse.class);
                response.setContact(contact);
            }

            if (cacheMap.containsKey("relationship")) {
                RelationshipResponse relationship = mapper.convertValue(cacheMap.get("relationship"), RelationshipResponse.class);
                response.setRelationship(relationship);
            }

            if (cacheMap.containsKey("familyMembers")) {
                List<FamilyMemberResponse> family = mapper.convertValue(
                        cacheMap.get("familyMembers"), new TypeReference<List<FamilyMemberResponse>>() {});
                response.setFamilyMembers(family);
            }

            if (cacheMap.containsKey("detail")) {
                DetailResponse detail = mapper.convertValue(cacheMap.get("detail"), DetailResponse.class);
                response.setDetail(detail);
            }

            response.setCreatedAt((String) cacheMap.get("createdAt"));
            response.setUpdatedAt((String) cacheMap.get("updatedAt"));

            logProducer.sendLog("user-service",
                    "INFO",
                    "[ProfileDetailCache] Successfully retrieved profile detail from cache for userId: " + userId);
            return response;

        } catch (Exception e) {
            logProducer.sendLog("user-service",
                    "ERROR",
                    "[ProfileDetailCache] Failed to fetch profile detail from cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteCachedProfileDetail(String userId) {
        String cacheKey = PROFILE_DETAIL_CACHE_PREFIX + userId;

        logProducer.sendLog("user-service",
                "DEBUG",
                "[ProfileDetailCache] Deleting profile detail cache for userId: " + userId);
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                logProducer.sendLog("user-service",
                        "INFO",
                        "[ProfileDetailCache] Successfully deleted profile detail cache for userId: " + userId);
            } else {
                logProducer.sendLog("user-service",
                        "INFO",
                        "[ProfileDetailCache] No profile detail cache found to delete for userId: " + userId);
            }
        } catch (Exception e) {
            logProducer.sendLog("user-service",
                    "ERROR",
                    "[ProfileDetailCache] Failed to delete profile detail cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void cachePublicProfileDetail(String userId, PublicProfileDetailsResponse response) {
        String cacheKey = PUBLIC_PROFILE_DETAIL_CACHE_PREFIX + userId;
        logProducer.sendLog("user-service",
                "DEBUG",
                "[FullProfileCache] Saving full profile detail to cache for userId: " + userId);
        try {
            Map<String, Object> cacheMap = mapper.convertValue(response, new TypeReference<Map<String, Object>>() {});

            redisTemplate.opsForValue().set(cacheKey, cacheMap, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            logProducer.sendLog("user-service",
                    "INFO",
                    "[FullProfileCache] Successfully saved full profile detail to cache: " + cacheKey);
        } catch (Exception e) {
            logProducer.sendLog("user-service",
                    "ERROR",
                    "[FullProfileCache] Failed to save full profile detail to cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public PublicProfileDetailsResponse getCachePublicProfileDetail(String userId) {
        String cacheKey = PUBLIC_PROFILE_DETAIL_CACHE_PREFIX + userId;
        logProducer.sendLog("user-service",
                "DEBUG",
                "[FullProfileCache] Fetching full profile detail from cache for userId: " + userId);
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                logProducer.sendLog("user-service",
                        "INFO",
                        "[FullProfileCache] No full profile detail cache found for userId: " + userId);
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> cacheMap = (Map<String, Object>) cached;
            PublicProfileDetailsResponse response = mapper.convertValue(cacheMap, PublicProfileDetailsResponse.class);

            logProducer.sendLog("user-service",
                    "INFO",
                    "[FullProfileCache] Successfully retrieved full profile detail from cache for userId: " + userId);
            return response;
        } catch (Exception e) {
            logProducer.sendLog("user-service",
                    "ERROR",
                    "[FullProfileCache] Failed to fetch full profile detail from cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void deletePublicCachedProfileDetail(String userId) {
        String cacheKey = PUBLIC_PROFILE_DETAIL_CACHE_PREFIX + userId;
        logProducer.sendLog("user-service",
                "DEBUG",
                "[ProfileDetailCache] Deleting profile detail cache for userId: " + userId);
        try {
            Boolean deleted = redisTemplate.delete(cacheKey);
            if (Boolean.TRUE.equals(deleted)) {
                logProducer.sendLog("user-service",
                        "INFO",
                        "[ProfileDetailCache] Successfully deleted profile detail cache for userId: " + userId);
            } else {
                logProducer.sendLog("user-service",
                        "INFO",
                        "[ProfileDetailCache] No profile detail cache found to delete for userId: " + userId);
            }
        } catch (Exception e) {
            logProducer.sendLog("user-service",
                    "ERROR",
                    "[ProfileDetailCache] Failed to delete profile detail cache for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }
}
