package com.example.trending_service.service.interfaces;

import java.util.List;

public interface ITrendingQueryService {
    List<String> getTopTrendingPosts(int limit);
}
