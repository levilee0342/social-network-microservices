package com.example.post_service.repository.redis.impl;

import com.example.post_service.repository.redis.IRedisHashtagSearch;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RedisHashtagSearchImpl implements IRedisHashtagSearch {

    private final JedisPool jedisPool;
    private static final String SORTED_SET_KEY = "hashtag:sorted";

    public RedisHashtagSearchImpl(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public Set<String> suggestHashtags(String prefix) {
        if (prefix == null || prefix.isBlank()) return Collections.emptySet();
        prefix = prefix.toLowerCase();
        if (!prefix.startsWith("#")) {
            prefix = "#" + prefix;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> results = jedis.zrangeByLex(SORTED_SET_KEY, "[" + prefix, "[" + prefix + "\uffff", 0, 10);
            return new LinkedHashSet<>(results);
        }
    }
}
