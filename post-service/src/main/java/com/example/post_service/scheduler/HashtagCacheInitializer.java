package com.example.post_service.scheduler;

import com.example.post_service.entity.Hashtag;
import com.example.post_service.repository.HashtagRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

@Service
public class HashtagCacheInitializer {

    private final JedisPool jedisPool;
    private final HashtagRepository hashtagRepository;

    public HashtagCacheInitializer(JedisPool jedisPool, HashtagRepository hashtagRepository) {
        this.jedisPool = jedisPool;
        this.hashtagRepository = hashtagRepository;
    }

    @PostConstruct
    public void syncHashtagCacheOnStartup() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del("hashtag:sorted");
            List<Hashtag> hashtags = hashtagRepository.findAll();
            for (Hashtag tag : hashtags) {
                String key = tag.getName().toLowerCase();
                if (!key.startsWith("#")) {
                    key = "#" + key;
                }
                jedis.zadd("hashtag:sorted", 0, key);
            }
            System.out.println("[Redis] Synced " + hashtags.size() + " hashtags from DB to Redis.");
        } catch (Exception e) {
            System.err.println("[Redis] Failed to sync hashtag cache: " + e.getMessage());
        }
    }

}
