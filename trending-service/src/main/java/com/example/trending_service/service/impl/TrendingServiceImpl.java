package com.example.trending_service.service.impl;

import com.example.trending_service.dto.request.TrendingEventRequest;
import com.example.trending_service.dto.response.PostResponse;
import com.example.trending_service.entity.TrendingEvents;
import com.example.trending_service.repository.TrendingEventRepository;
import com.example.trending_service.repository.IRedisTrendingUpdater;
import com.example.trending_service.service.interfaces.ITrendingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class TrendingServiceImpl implements ITrendingService {
    private final TrendingEventRepository trendingEventRepository;
    private final IRedisTrendingUpdater redisTrendingUpdater;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public TrendingServiceImpl(TrendingEventRepository trendingEventRepository,
                               IRedisTrendingUpdater redisTrendingUpdater,
                               RedisTemplate<String, Object> redisTemplate,
                               ObjectMapper objectMapper){
        this.trendingEventRepository = trendingEventRepository;
        this.redisTrendingUpdater = redisTrendingUpdater;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void handleTrendingEvent(TrendingEventRequest request) {
        try {
            TrendingEvents event = TrendingEvents.builder()
                    .postId(request.getPostId())
                    .eventType(request.getEventType())
                    .occurredAt(Optional.ofNullable(request.getOccurredAt()).orElse(LocalDateTime.now()))
                    .createdAt(Optional.ofNullable(request.getCreatedAt()).orElse(LocalDateTime.now()))
                    .build();
            trendingEventRepository.save(event);
            redisTrendingUpdater.updateStatsAndScore(
                    request.getPostId(),
                    request.getEventType(),
                    event.getCreatedAt(),
                    request.getToken()
            );
        } catch (Exception ex) {
            log.error("Error handling trending event for post {}: {}", request.getPostId(), ex.getMessage(), ex);
        }
    }

    @Override
    public List<PostResponse> getTrendingPostsFromCache() {
        Set<Object> rawPostIds = redisTemplate.opsForZSet()
                .reverseRange("trending:posts", 0, -1);

        if (rawPostIds == null || rawPostIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<PostResponse> result = new ArrayList<>();
        for (Object rawId : rawPostIds) {
            String postId = rawId.toString().replace("\"", "");
            String cacheKey = "trending:post:" + postId;

            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) continue;

            if (cached instanceof Map<?, ?> map) {
                result.add(objectMapper.convertValue(map, PostResponse.class));
            } else if (cached instanceof PostResponse post) {
                result.add(post);
            }
        }
        return result;
    }
}


