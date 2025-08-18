package com.example.post_service.service.impl;

import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.service.interfaces.INewsfeedService;
import com.example.post_service.service.interfaces.IPostNewFeedMapperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NewsfeedServiceImpl implements INewsfeedService {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final IPostNewFeedMapperService postNewFeedMapperService;
    private final ProducerLog logProducer;

    public NewsfeedServiceImpl(JdbcTemplate jdbcTemplate,
                               RedisTemplate<String, String> redisTemplate,
                               ObjectMapper objectMapper,
                               IPostNewFeedMapperService postNewFeedMapperService,
                               ProducerLog logProducer) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.postNewFeedMapperService = postNewFeedMapperService;
        this.logProducer = logProducer;
    }

    @Override
    public List<PostResponse> getNewsfeed(String userId, List<String> friendIds, int limit, int offset) {
        // Gọi stored procedure từ PostgreSQL
        List<PostResponse> dbPosts = jdbcTemplate.query(
                "SELECT * FROM get_newsfeed_posts(?, ?, ?, ?)",
                new Object[]{userId, friendIds.toArray(new String[0]), limit, offset},
                (rs, rowNum) -> postNewFeedMapperService.mapRowToPostNewFeedResponse(rs)
        );
        // Lấy danh sách trending post từ Redis
        List<String> keys = scanKeys("trending:post:*");
        List<PostResponse> trendingPosts = new ArrayList<>();
        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                try {
                    PostResponse post = objectMapper.readValue(json, PostResponse.class);
                    trendingPosts.add(post);
                } catch (JsonProcessingException e) {
                    logProducer.sendLog(
                            "post-service",
                            "ERROR",
                            "Failed to parse Json: " + e
                    );
                    throw new AppException(AuthErrorCode.FAILED_PARSE_JSON);
                }
            }
        }
        // Gộp 2 danh sách
        List<PostResponse> combined = new ArrayList<>();
        combined.addAll(trendingPosts);
        combined.addAll(dbPosts);
        // Sort theo createdAt giảm dần
        combined = combined.stream()
                .filter(p -> p != null && p.getCreatedAt() != null)
                .sorted(Comparator.comparingLong(PostResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());
        return combined;
    }

    private List<String> scanKeys(String pattern) {
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);

        List<String> result = new ArrayList<>();
        while (cursor.hasNext()) {
            result.add(new String(cursor.next(), StandardCharsets.UTF_8));
        }
        return result;
    }

}

