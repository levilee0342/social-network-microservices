package com.example.trending_service.repository.impl;

import com.example.trending_service.dto.response.ApiResponse;
import com.example.trending_service.dto.response.PostResponse;
import com.example.trending_service.kafka.producer.LogProducer;
import com.example.trending_service.repository.IRedisTrendingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Repository
public class RedisTrendingRepositoryImpl implements IRedisTrendingRepository {

    private static final String TRENDING_ZSET_KEY = "trending:posts";
    private static final String POST_CACHE_KEY_PREFIX = "trending:post:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LogProducer logProducer;

    public RedisTrendingRepositoryImpl(RedisTemplate<String, Object> redisTemplate,
                                       RestTemplate restTemplate,
                                       ObjectMapper objectMapper,
                                       LogProducer logProducer) {
        this.redisTemplate = redisTemplate;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.logProducer = logProducer;
    }


    @Override
    public void addToTrending(String postId, double score) {
        redisTemplate.opsForZSet().add(TRENDING_ZSET_KEY, String.valueOf(postId), score);
    }

    @Override
    public void removeFromTrending(String postId) {
        redisTemplate.opsForZSet().remove(TRENDING_ZSET_KEY, postId);
        redisTemplate.delete(POST_CACHE_KEY_PREFIX + postId);
    }

    @Override
    public void cachePostById(String postId, String token) {
        try {
            String url = "http://post-service/posts/" + postId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<ApiResponse<PostResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                PostResponse post = response.getBody().getResult();
                String key = POST_CACHE_KEY_PREFIX + postId;
                Map<String, Object> cacheMap = objectMapper.convertValue(post, Map.class);
                redisTemplate.opsForValue().set(key, cacheMap, 48, TimeUnit.HOURS);
                logProducer.sendLog(
                        "trending-service",
                        "DEBUG",
                        "Successfully cached post: " + post
                );
            }
        } catch (Exception e) {
            logProducer.sendLog(
                    "trending-service",
                    "DEBUG",
                    "Failed to fetch/cache post " + postId + ": " + e.getMessage()
            );
        }
    }

    @Override
    public List<String> getTopTrendingPostIds(int limit) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<Object> result = zSetOps.reverseRange(TRENDING_ZSET_KEY, 0, limit - 1);
        if (result == null) return List.of();
        return result.stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public List<PostResponse> getTopTrendingPosts(int limit) {
        List<String> postIds = getTopTrendingPostIds(limit);
        List<Object> cachedObjects = redisTemplate.opsForValue().multiGet(
                postIds.stream().map(id -> POST_CACHE_KEY_PREFIX + id).collect(Collectors.toList())
        );

        return cachedObjects.stream()
                .filter(obj -> obj instanceof PostResponse)
                .map(obj -> (PostResponse) obj)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getTrendingPostIds() {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Set<Object> rawSet = zSetOps.range(TRENDING_ZSET_KEY, 0, -1);
        return rawSet.stream().map(Object::toString).collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public void updateScore(String postId, double score) {
        redisTemplate.opsForZSet().add(TRENDING_ZSET_KEY, postId, score);
    }
}

