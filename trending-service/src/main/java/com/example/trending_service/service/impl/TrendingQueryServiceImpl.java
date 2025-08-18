package com.example.trending_service.service.impl;

import com.example.trending_service.repository.IRedisTrendingRepository;
import com.example.trending_service.service.interfaces.ITrendingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendingQueryServiceImpl implements ITrendingQueryService {

    private final IRedisTrendingRepository redisTrendingRepository;

    @Override
    public List<String> getTopTrendingPosts(int limit) {
        return redisTrendingRepository.getTopTrendingPostIds(limit);
    }
}

