package com.example.post_service.service;

import com.example.post_service.repository.redis.IRedisHashtagSearch;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HashtagService {

    private final IRedisHashtagSearch redisHashtagSearch;

    public HashtagService(IRedisHashtagSearch redisHashtagSearch) {
        this.redisHashtagSearch = redisHashtagSearch;
    }

    public Set<String> suggestHashtags(String prefix) {
        return redisHashtagSearch.suggestHashtags(prefix);
    }
}
