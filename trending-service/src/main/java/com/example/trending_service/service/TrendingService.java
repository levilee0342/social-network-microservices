package com.example.trending_service.service;

import com.example.trending_service.dto.response.PostResponse;
import com.example.trending_service.service.interfaces.ITrendingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrendingService {

    private final ITrendingService trendingService;

    public TrendingService(ITrendingService trendingService) {
        this.trendingService = trendingService;
    }

    public List<PostResponse> getTrendingPostsFromCache(){
        return trendingService.getTrendingPostsFromCache();
    }
}
