package com.example.post_service.service.impl;

import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.dto.response.PublicProfileResponse;
import com.example.post_service.entity.Hashtag;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostImage;
import com.example.post_service.repository.redis.IRedisPublicProfile;
import com.example.post_service.service.interfaces.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostMapperServiceImpl implements IPostMapperService {

    private final IRedisPublicProfile redisPublicProfile;
    private final IReactionService reactionService;
    private final ICommentMapperService commentMapperService;

    public PostMapperServiceImpl(IRedisPublicProfile redisPublicProfile,
                                 IReactionService reactionService,
                                 ICommentMapperService commentMapperService) {
        this.redisPublicProfile = redisPublicProfile;
        this.reactionService = reactionService;
        this.commentMapperService = commentMapperService;
    }

    @Override
    public PostResponse mapToPostResponse(Post post) {
        PublicProfileResponse profile = redisPublicProfile.getPublicProfileByUserId(post.getUserId());
        PostResponse response = new PostResponse();
        if (post.getVisibility() != Post.PostVisibility.ANONYMOUS) {
            response.setUserId(post.getUserId());
            response.setAuthorName(profile != null ? profile.getFullName() : "Unknown");
            response.setAuthorAvatarUrl(profile != null ? profile.getAvatarUrl() : null);
        } else {
            response.setUserId("Unknown");
            response.setAuthorName("Anonymous User");
            response.setAuthorAvatarUrl("anonymous.com");
        }
        response.setPostId(post.getPostId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setVoiceUrl(post.getVoiceUrl());
        response.setVoiceDuration(post.getVoiceDuration());
        response.setImageUrls(post.getImages().stream().map(PostImage::getImageUrl).toList());
        response.setHashtags(post.getHashtags().stream().map(Hashtag::getName).toList());
        response.setVisibility(post.getVisibility());
        response.setReactionCount(reactionService.countReactionsByPostId(post.getPostId()));
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        //Thêm danh sách comment
        List<CommentResponse> commentResponses = post.getComments().stream()
                .limit(5)
                .map(commentMapperService::mapToCommentResponse)
                .toList();
        response.setComments(commentResponses);
        return response;
    }

    @Override
    public PostResponse mapToPostShareResponse(Post post, Long shareId, Long sharedAt) {
        PostResponse response = mapToPostResponse(post);
        response.setShareId(shareId);
        response.setShareCreateAt(sharedAt);
        return response;
    }
}
