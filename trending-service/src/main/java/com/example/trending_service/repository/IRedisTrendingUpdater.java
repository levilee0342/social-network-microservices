package com.example.trending_service.repository;

import java.time.LocalDateTime;

public interface IRedisTrendingUpdater {
    void updateStatsAndScore(Long postId, String eventType, LocalDateTime eventCreatedAt, String token);
}