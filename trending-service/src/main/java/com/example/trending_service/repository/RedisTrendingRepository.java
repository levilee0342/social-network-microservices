package com.example.trending_service.repository;

import com.example.trending_service.dto.response.PostResponse;

import java.util.List;
import java.util.Set;

public interface RedisTrendingRepository {
    void addToTrending(String postId, double score);
    void removeFromTrending(String postId);
    void cachePostById(String postId, String token);
    List<String> getTopTrendingPostIds(int limit);
    List<PostResponse> getTopTrendingPosts(int limit);
    Set<String> getTrendingPostIds();
    void updateScore(String postId, double score);
}
