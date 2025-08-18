package com.example.post_service.service.impl;

import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.dto.response.PublicProfileResponse;
import com.example.post_service.entity.Comment;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.repository.redis.IRedisPublicProfile;
import com.example.post_service.service.interfaces.ICommentMapperService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentMapperServiceImpl implements ICommentMapperService {

    private final IRedisPublicProfile redisPublicProfile;
    private final ProducerLog producerLog;

    public CommentMapperServiceImpl(IRedisPublicProfile redisPublicProfile, ProducerLog producerLog) {
        this.redisPublicProfile = redisPublicProfile;
        this.producerLog = producerLog;
    }

    @Override
    public CommentResponse mapToCommentResponse(Comment comment) {
        try {
            PublicProfileResponse profile = redisPublicProfile.getPublicProfileByUserId(comment.getUserId());
            CommentResponse response = new CommentResponse();
            response.setCommentId(comment.getCommentId());
            // Safe null-checks
            if (comment.getPost() != null) {
                response.setPostId(comment.getPost().getPostId());
            }
            if (comment.getPostShare() != null) {
                response.setShareId(comment.getPostShare().getShareId());
            }
            response.setUserId(comment.getUserId());
            response.setAuthorName(profile != null ? profile.getFullName() : "Unknown");
            response.setAuthorAvatarUrl(profile != null ? profile.getAvatarUrl() : null);
            response.setContent(comment.getContent());
            response.setImageUrl(comment.getImageUrl());
            response.setCreatedAt(comment.getCreatedAt());
            response.setUpdatedAt(comment.getUpdatedAt());
            // Count likes
            response.setReactionCount(comment.getLikes() != null ? comment.getLikes().size() : 0);
            // Recursive mapping for replies
            List<CommentResponse> replyResponses = comment.getReplies() != null
                    ? comment.getReplies().stream()
                    .map(this::mapToCommentResponse)
                    .collect(Collectors.toList())
                    : List.of();
            response.setReplies(replyResponses);
            return response;
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "Unable to map comment: " + (comment != null ? comment.getCommentId() : "null") + ": " + e.getMessage()
            );
            throw new AppException(AuthErrorCode.FAILED_MAP_COMMENT);
        }
    }
}
