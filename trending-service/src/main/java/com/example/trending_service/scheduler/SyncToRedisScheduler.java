package com.example.trending_service.scheduler;

import com.example.trending_service.repository.IRedisTrendingRepository;
import com.example.trending_service.utils.TrendingScoreCalculator;
import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class SyncToRedisScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IRedisTrendingRepository redisTrendingRepository;
    private final JdbcTemplate jdbcTemplate;
    private final TrendingScoreCalculator scoreCalculator;

    private static final String STATS_KEY_PREFIX = "post:";
    private static final String STATS_KEY_SUFFIX = ":stats";

    public SyncToRedisScheduler(RedisTemplate<String, Object> redisTemplate,
                                IRedisTrendingRepository redisTrendingRepository,
                                JdbcTemplate jdbcTemplate,
                                TrendingScoreCalculator scoreCalculator) {
        this.redisTemplate = redisTemplate;
        this.redisTrendingRepository = redisTrendingRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.scoreCalculator = scoreCalculator;
    }

    @PostConstruct
    public void syncAllToRedis() {
        System.out.println("Initializing Redis with scores from database...");
        String allStatsSql = "SELECT * FROM get_trending_post_stats()";
        jdbcTemplate.query(allStatsSql, (ResultSetExtractor<Void>) rs -> {
            HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
            while (rs.next()) {
                Long postId = rs.getLong("p_id");
                LocalDateTime createdAt = rs.getTimestamp("p_created").toLocalDateTime();
                long likes = rs.getLong("p_likes");
                long comments = rs.getLong("p_comments");
                long shares = rs.getLong("p_shares");
                String key = STATS_KEY_PREFIX + postId + STATS_KEY_SUFFIX;
                Map<String, Object> postStats = new HashMap<>();
                postStats.put("createdAt", createdAt.toString());
                postStats.put("likes", likes);
                postStats.put("comments", comments);
                postStats.put("shares", shares);
                hashOps.putAll(key, postStats);
                long totalInteractions = likes + comments + shares;
                double score = scoreCalculator.calculateScore(likes, comments, shares, createdAt);
                if (scoreCalculator.isTrending(score, totalInteractions)) {
                    redisTrendingRepository.addToTrending(postId.toString(), score);
                } else {
                    redisTrendingRepository.removeFromTrending(postId.toString());
                }
            }
            return null;
        });
        System.out.println("Redis initialization complete.");
    }
}
