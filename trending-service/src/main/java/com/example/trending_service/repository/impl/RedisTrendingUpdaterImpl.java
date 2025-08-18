package com.example.trending_service.repository.impl;

import com.example.trending_service.repository.IRedisTrendingRepository;
import com.example.trending_service.repository.IRedisTrendingUpdater;
import com.example.trending_service.utils.ParseUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RedisTrendingUpdaterImpl implements IRedisTrendingUpdater {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IRedisTrendingRepository redisTrendingRepository;
    private final ParseUtils parseUtils;

    private static final String STATS_KEY_PREFIX = "post:";
    private static final String STATS_KEY_SUFFIX = ":stats";
    private static final double LIKE_WEIGHT = 0.5;
    private static final double COMMENT_WEIGHT = 2.0;
    private static final double SHARE_WEIGHT = 3.0;
    private static final double DECAY_GRAVITY = 1.3;
    private static final double POPULARITY_SCORE_THRESHOLD = 3.0;
    private static final int POPULARITY_INTERACTION_THRESHOLD = 5;

    public RedisTrendingUpdaterImpl(RedisTemplate<String, Object> redisTemplate,
                                    IRedisTrendingRepository redisTrendingRepository,
                                    ParseUtils parseUtils) {
        this.redisTemplate = redisTemplate;
        this.redisTrendingRepository = redisTrendingRepository;
        this.parseUtils = parseUtils;
    }

    @Override
    public void updateStatsAndScore(Long postId, String eventType, LocalDateTime eventCreatedAt, String token) {
        String key = STATS_KEY_PREFIX + postId + STATS_KEY_SUFFIX;
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();

        if ("CREATE_POST".equals(eventType)) {
            hashOps.put(key, "createdAt", eventCreatedAt.toString());
            hashOps.put(key, "likes", 0);
            hashOps.put(key, "comments", 0);
            hashOps.put(key, "shares", 0);
            return;
        }

        switch (eventType) {
            case "NEW_LIKE" -> hashOps.increment(key, "likes", 1);
            case "UN_LIKE" -> hashOps.increment(key, "likes", -1);
            case "NEW_COMMENT" -> hashOps.increment(key, "comments", 1);
            case "DELETE_COMMENT" -> hashOps.increment(key, "comments", -1);
            case "NEW_SHARE" -> hashOps.increment(key, "shares", 1);
            case "DELETE_SHARE" -> hashOps.increment(key, "shares", -1);
            default -> { return; }
        }

        // Lấy lại stats sau khi cập nhật
        List<Object> stats = hashOps.multiGet(key, List.of("likes", "comments", "shares", "createdAt"));
        long likes = parseUtils.parseLong(stats.get(0));
        long comments = parseUtils.parseLong(stats.get(1));
        long shares = parseUtils.parseLong(stats.get(2));
        Object createdAtObj = stats.get(3);

        if (createdAtObj == null) {
            System.err.println("Could not find creation time for post " + postId + ". Cannot calculate score.");
            return;
        }

        LocalDateTime postCreatedAt = LocalDateTime.parse(createdAtObj.toString());
        long totalInteractions = likes + comments + shares;

        double hours_since = Duration.between(postCreatedAt, LocalDateTime.now()).toHours();
        if (hours_since < 0) hours_since = 0;

        double decay = 1 / Math.pow(hours_since + 2, DECAY_GRAVITY);
        double score = (likes * LIKE_WEIGHT + comments * COMMENT_WEIGHT + shares * SHARE_WEIGHT) * decay;

        System.out.printf("📊 Post: %d, Score: %.2f, Likes: %d, Comments: %d, Shares: %d, Decay: %.4f, Hours: %.2f%n",
                postId, score, likes, comments, shares, decay, hours_since);

        if (score > POPULARITY_SCORE_THRESHOLD && totalInteractions >= POPULARITY_INTERACTION_THRESHOLD) {
            System.out.printf("🚀 Post %d is trending with score %.2f. Adding to ZSET and caching...%n", postId, score);
            redisTrendingRepository.addToTrending(postId.toString(), score);
            redisTrendingRepository.cachePostById(postId.toString(), token);
        } else {
            System.out.printf("📉 Post %d with score %.2f is no longer trending. Removing from ZSET and cache.%n", postId, score);
            redisTrendingRepository.removeFromTrending(postId.toString());
        }
    }

}
