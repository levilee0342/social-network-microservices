package com.example.message_service.repository.impl;

import com.example.message_service.dto.request.AnonymousChatRequest;
import com.example.message_service.error.AuthErrorCode;
import com.example.message_service.exception.AppException;
import com.example.message_service.repository.IRedisAnonymousChatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class RedisAnonymousChatRepositoryImpl implements IRedisAnonymousChatRepository {

    private static final String ANONYMOUS_CHAT_KEY_PREFIX = "anonymous:chat:request:";
    private static final String PENDING_REQUESTS_SET_PREFIX = "pending:chat:requests:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisAnonymousChatRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(AnonymousChatRequest request) {
        String key = ANONYMOUS_CHAT_KEY_PREFIX + request.getUserId();
        try {
            redisTemplate.opsForValue().set(key, request, DEFAULT_TTL);
            if (!request.isMatched()) {
                // Thêm vào set chung
                redisTemplate.opsForZSet().add(PENDING_REQUESTS_SET_PREFIX + "all", request.getUserId(), request.getTimestamp());
                // Giới tính dạng boolean: true = male, false = female
                if (request.isGender()) {
                    redisTemplate.opsForZSet().add(PENDING_REQUESTS_SET_PREFIX + "male", request.getUserId(), request.getTimestamp());
                } else {
                    redisTemplate.opsForZSet().add(PENDING_REQUESTS_SET_PREFIX + "female", request.getUserId(), request.getTimestamp());
                }
            } else {
                removeFromPendingSets(request.getUserId());
            }
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.FAILED_SAVE_REDIS);
        }
    }

    @Override
    public Optional<AnonymousChatRequest> findById(String userId) {
        String key = ANONYMOUS_CHAT_KEY_PREFIX + userId;
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.convertValue(raw, AnonymousChatRequest.class));
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.FAILED_RETRIEVE_REDIS);
        }
    }

    @Override
    public List<AnonymousChatRequest> findPotentialMatches(AnonymousChatRequest currentRequest, int limit) {
        List<AnonymousChatRequest> potentialMatches = new ArrayList<>();
        String targetGenderSetKey;
        // Xác định set nào để tìm kiếm dựa trên giới tính của người yêu cầu
        if (currentRequest.isGender()) { // true = male → tìm female
            targetGenderSetKey = PENDING_REQUESTS_SET_PREFIX + "female";
        } else { // false = female → tìm male
            targetGenderSetKey = PENDING_REQUESTS_SET_PREFIX + "male";
        }
        try {
            // Lấy một số lượng giới hạn các userId từ Sorted Set
            Set<Object> userIds = redisTemplate.opsForZSet().range(targetGenderSetKey, 0, limit);
            if (userIds != null && !userIds.isEmpty()) {
                List<String> keysToFetch = userIds.stream()
                        .filter(id -> !id.equals(currentRequest.getUserId())) // Loại bỏ chính nó
                        .map(id -> ANONYMOUS_CHAT_KEY_PREFIX + id)
                        .collect(Collectors.toList());
                if (!keysToFetch.isEmpty()) {
                    List<Object> rawRequests = redisTemplate.opsForValue().multiGet(keysToFetch);
                    if (rawRequests != null) {
                        for (Object raw : rawRequests) {
                            if (raw != null) {
                                AnonymousChatRequest request = objectMapper.convertValue(raw, AnonymousChatRequest.class);
                                if (request != null && !request.isMatched()) {
                                    potentialMatches.add(request);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.FAILED_RETRIEVE_REDIS);
        }
        return potentialMatches;
    }

    private void removeFromPendingSets(String userId) {
        redisTemplate.opsForZSet().remove(PENDING_REQUESTS_SET_PREFIX + "all", userId);
        redisTemplate.opsForZSet().remove(PENDING_REQUESTS_SET_PREFIX + "male", userId);
        redisTemplate.opsForZSet().remove(PENDING_REQUESTS_SET_PREFIX + "female", userId);
    }
}
