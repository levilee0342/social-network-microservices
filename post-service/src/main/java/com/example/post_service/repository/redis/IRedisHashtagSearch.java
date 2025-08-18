package com.example.post_service.repository.redis;

import java.util.Set;

public interface IRedisHashtagSearch {
    Set<String> suggestHashtags(String prefix);
}
