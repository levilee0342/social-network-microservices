package com.example.trending_service.scheduler;

import com.example.trending_service.repository.IRedisTrendingRepository;
import com.example.trending_service.utils.ParseUtils;
import com.example.trending_service.utils.TrendingScoreCalculator;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TrendingDecayJob {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IRedisTrendingRepository redisTrendingRepository;
    private final ParseUtils parseUtils;
    private final TrendingScoreCalculator scoreCalculator;

    private static final String STATS_KEY_PREFIX = "post:";
    private static final String STATS_KEY_SUFFIX = ":stats";

    public TrendingDecayJob(RedisTemplate<String, Object> redisTemplate,
                            IRedisTrendingRepository redisTrendingRepository,
                            ParseUtils parseUtils,
                            TrendingScoreCalculator scoreCalculator) {
        this.redisTemplate = redisTemplate;
        this.redisTrendingRepository = redisTrendingRepository;
        this.parseUtils = parseUtils;
        this.scoreCalculator = scoreCalculator;
    }

    @Scheduled(fixedRate = 3600000)
    public void recalculateTrendingScores() {
        Set<String> trendingPostIds = redisTrendingRepository.getTrendingPostIds();
        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        for (String postId : trendingPostIds) {
            String key = STATS_KEY_PREFIX + postId + STATS_KEY_SUFFIX;
            List<Object> stats = hashOps.multiGet(key, List.of("likes", "comments", "shares", "createdAt"));
            if (stats.contains(null)) continue;
            long likes = parseUtils.parseLong(stats.get(0));
            long comments = parseUtils.parseLong(stats.get(1));
            long shares = parseUtils.parseLong(stats.get(2));
            LocalDateTime createdAt = LocalDateTime.parse(stats.get(3).toString());
            long totalInteractions = likes + comments + shares;
            double score = scoreCalculator.calculateScore(likes, comments, shares, createdAt);
            System.out.printf("Recalculated Post %s: score=%.2f (interactions=%d)%n", postId, score, totalInteractions);
            if (!scoreCalculator.isTrending(score, totalInteractions)) {
                System.out.printf("Removing Post %s from trending due to low score.%n", postId);
                redisTrendingRepository.removeFromTrending(postId);
            } else {
                redisTrendingRepository.updateScore(postId, score);
            }
        }
    }
}
