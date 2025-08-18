package com.example.trending_service.utils;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class TrendingScoreCalculator {

    private static final double LIKE_WEIGHT = 0.5;
    private static final double COMMENT_WEIGHT = 2.0;
    private static final double SHARE_WEIGHT = 3.0;
    private static final double DECAY_GRAVITY = 1.3;
    private static final double POPULARITY_SCORE_THRESHOLD = 3.0;
    private static final int POPULARITY_INTERACTION_THRESHOLD = 5;

    public double calculateScore(long likes, long comments, long shares, LocalDateTime createdAt) {
        double hoursSince = Duration.between(createdAt, LocalDateTime.now()).toHours();
        if (hoursSince < 0) hoursSince = 0;
        double decay = 1 / Math.pow(hoursSince + 2, DECAY_GRAVITY);
        return (likes * LIKE_WEIGHT + comments * COMMENT_WEIGHT + shares * SHARE_WEIGHT) * decay;
    }

    public boolean isTrending(double score, long interactions) {
        return score > POPULARITY_SCORE_THRESHOLD && interactions >= POPULARITY_INTERACTION_THRESHOLD;
    }
}
