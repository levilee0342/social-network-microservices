package com.example.post_service.service.impl;

import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.dto.response.PublicProfileResponse;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.repository.redis.IRedisPublicProfile;
import com.example.post_service.service.interfaces.IPostNewFeedMapperService;
import com.example.post_service.utils.ParseUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

@Service
public class NewFeedMapperServiceImpl implements IPostNewFeedMapperService {

    private ObjectMapper mapper;
    private final IRedisPublicProfile redisPublicProfile;
    private final ProducerLog logProducer;

    public NewFeedMapperServiceImpl(IRedisPublicProfile redisPublicProfile, ProducerLog logProducer) {
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.redisPublicProfile = redisPublicProfile;
        this.logProducer = logProducer;
    }

    @Override
    public PostResponse mapRowToPostNewFeedResponse(ResultSet rs) throws SQLException {
        try {
            String postJson = rs.getString(1);
            PostResponse post = mapper.readValue(postJson, PostResponse.class);

            if (post.getComments() == null) {
                post.setComments(Collections.emptyList());
            }
            if (post.getHashtags() == null) {
                post.setHashtags(Collections.emptyList());
            }
            if (post.getImageUrls() == null) {
                post.setImageUrls(Collections.emptyList());
            }
            // Lấy tên + avatar user đăng bài
            PublicProfileResponse profileAuth = redisPublicProfile.getPublicProfileByUserId(post.getUserId());
            if (profileAuth != null) {
                post.setAuthorName(profileAuth.getFullName());
                post.setAuthorAvatarUrl(profileAuth.getAvatarUrl());
            }
            // Lấy tên + avatar user comment
            if (!post.getComments().isEmpty()) {
                for (CommentResponse comment : post.getComments()) {
                    PublicProfileResponse profileCommenter = redisPublicProfile.getPublicProfileByUserId(comment.getUserId());
                    if (profileCommenter != null) {
                        comment.setAuthorName(profileCommenter.getFullName());
                        comment.setAuthorAvatarUrl(profileCommenter.getAvatarUrl());
                    }
                }
            }
            return post;
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[New Feed] Unable to map new feed: " + e.getMessage()
            );
            throw new AppException(AuthErrorCode.FAILED_MAP_NEW_FEED);
        }
    }
}
