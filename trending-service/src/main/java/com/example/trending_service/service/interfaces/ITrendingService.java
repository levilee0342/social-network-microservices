package com.example.trending_service.service.interfaces;

import com.example.trending_service.dto.request.TrendingEventRequest;
import com.example.trending_service.dto.response.PostResponse;

import java.util.List;

public interface ITrendingService {
    void handleTrendingEvent(TrendingEventRequest request);
    List<PostResponse> getTrendingPostsFromCache();
}

