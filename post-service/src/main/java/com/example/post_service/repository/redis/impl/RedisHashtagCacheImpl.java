package com.example.post_service.repository.redis.impl;

import com.example.post_service.repository.redis.IRedisHashtagCache;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisHashtagCacheImpl implements IRedisHashtagCache {

    private final JedisPool jedisPool;
    private static final String USAGE_KEY = "hashtag:usage";
    private static final String SORTED_KEY = "hashtag:sorted";

    public RedisHashtagCacheImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public void updateHashtagCache(String rawName, boolean increaseScore) {
        if (rawName == null || rawName.isBlank()) return;
        String tag = rawName.startsWith("#") ? rawName.toLowerCase() : "#" + rawName.toLowerCase();
        try (Jedis jedis = jedisPool.getResource()) {
            if (increaseScore) {
                jedis.zincrby(USAGE_KEY, 1.0, tag);
            }
            jedis.zadd(SORTED_KEY, 0.0, tag); // Ghi vào hashtag:sorted
        }
    }
}
