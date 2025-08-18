package com.example.job_system_social.trending_score;

import com.example.job_system_social.model.TrendingEventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrendingScoreCalculator extends RichMapFunction<TrendingEventRequest, Void> {

    private static final Logger logger = LoggerFactory.getLogger(TrendingScoreCalculator.class);

    private transient Jedis redis;
    private transient ObjectMapper objectMapper;

    private static final String TRENDING_ZSET_KEY = "trending:posts";
    private static final String POST_CACHE_KEY_PREFIX = "trending:post:";
    private static final String STATS_KEY_PREFIX = "post:";
    private static final String STATS_KEY_SUFFIX = ":stats";
    private static final double LIKE_WEIGHT = 0.5;
    private static final double COMMENT_WEIGHT = 2.0;
    private static final double SHARE_WEIGHT = 3.0;
    private static final double DECAY_GRAVITY = 1.3;
    private static final double POPULARITY_SCORE_THRESHOLD = 3.0;
    private static final int POPULARITY_INTERACTION_THRESHOLD = 5;
    private static final long CACHE_TTL_HOURS = 48;

    @Override
    public void open(Configuration parameters) {
        redis = new Jedis("host.docker.internal", 6379);
        objectMapper = new ObjectMapper();
    }

    @Override
    public Void map(TrendingEventRequest event) {
        try {
            String statsKey = STATS_KEY_PREFIX + event.getPostId() + STATS_KEY_SUFFIX;
            String eventType = event.getEventType();

            if ("CREATE_POST".equals(eventType)) {
                Map<String, String> initialStats = new HashMap<>();
                initialStats.put("createdAt", event.getCreatedAt().toString());
                initialStats.put("likes", "0");
                initialStats.put("comments", "0");
                initialStats.put("shares", "0");
                redis.hset(statsKey, initialStats);
                return null;
            }

            String field = switch (eventType) {
                case "NEW_LIKE", "UN_LIKE" -> "likes";
                case "NEW_COMMENT", "DELETE_COMMENT" -> "comments";
                case "NEW_SHARE", "DELETE_SHARE" -> "shares";
                default -> null;
            };

            if (field == null) {
                logger.warn("Invalid event type: {} for post {}", eventType, event.getPostId());
                return null;
            }

            long delta = eventType.startsWith("DELETE") || eventType.equals("UN_LIKE") ? -1 : 1;
            redis.hincrBy(statsKey, field, delta);

            List<String> stats = redis.hmget(statsKey, "likes", "comments", "shares", "createdAt");
            if (stats.get(3) == null) {
                logger.error("Missing createdAt for post {}", event.getPostId());
                return null;
            }

            long likes = parseLongSafe(stats.get(0));
            long comments = parseLongSafe(stats.get(1));
            long shares = parseLongSafe(stats.get(2));
            LocalDateTime createdAt = LocalDateTime.parse(stats.get(3).replace("\"", ""));
            long totalInteractions = likes + comments + shares;

            double hoursSince = Math.max(0, Duration.between(createdAt, LocalDateTime.now()).toHours());
            double decay = 1 / Math.pow(hoursSince + 2, DECAY_GRAVITY);
            double score = (likes * LIKE_WEIGHT + comments * COMMENT_WEIGHT + shares * SHARE_WEIGHT) * decay;

            logger.debug("Post {} | Score: {:.2f} | Likes: {} | Comments: {} | Shares: {} | Decay: {:.4f} | Hours: {:.2f}",
                    event.getPostId(), score, likes, comments, shares, decay, hoursSince);

            String postIdStr = event.getPostId().toString();
            if (score > POPULARITY_SCORE_THRESHOLD && totalInteractions >= POPULARITY_INTERACTION_THRESHOLD) {
                redis.zadd(TRENDING_ZSET_KEY, score, postIdStr);
                cachePost(postIdStr, event.getToken());
                logger.info("Post {} is trending with score {:.2f}", postIdStr, score);
            } else {
                redis.zrem(TRENDING_ZSET_KEY, postIdStr);
                redis.del(POST_CACHE_KEY_PREFIX + postIdStr);
                logger.info("Post {} removed from trending", postIdStr);
            }
        } catch (Exception e) {
            logger.error("Error processing event for post {}: {}", event.getPostId(), e.getMessage(), e);
        }
        return null;
    }

    private void cachePost(String postId, String token) {
        try {
            String url = "http://post-service/posts/" + postId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String cacheKey = POST_CACHE_KEY_PREFIX + postId;
                redis.setex(cacheKey, (int) CACHE_TTL_HOURS * 3600, objectMapper.writeValueAsString(response.getBody()));
                logger.info("Successfully cached post: {}", postId);
            } else {
                logger.error("Failed to fetch post {}: HTTP {}", postId, response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Exception while caching post {}: {}", postId, e.getMessage(), e);
        }
    }

    private long parseLongSafe(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void close() {
        if (redis != null) redis.close();
    }
}
