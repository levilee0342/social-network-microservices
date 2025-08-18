package com.example.post_service.service;

import com.example.post_service.dto.request.PostRequest;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.interfaces.*;
import com.example.post_service.utils.HashtagRedisUpdater;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final IPostService postService;
    private final IFriendsManagerService friendsManagerService;
    private final INewsfeedService newsfeedService;

    public PostService(IPostService postService,
                       IFriendsManagerService friendsManagerService,
                       INewsfeedService newsfeedService) {
        this.postService = postService;
        this.friendsManagerService = friendsManagerService;
        this.newsfeedService = newsfeedService;
    }

    public List<PostResponse> getPosts(String requestingUserId, String targetUserId, HttpServletRequest httpRequest) {
        return postService.getPosts(requestingUserId, targetUserId, httpRequest);
    }

    public List<String> getFriendIds(String userId, String token){
        return friendsManagerService.getFriendIds(userId, token);
    }

    public List<PostResponse> getNewsfeed(String userId, List<String> friendIds, int limit, int offset){
        return newsfeedService.getNewsfeed(userId, friendIds, limit, offset);
    }

    public PostResponse getPostById(Long postId, String currentUserId, HttpServletRequest httpRequest) {
        return postService.getPostById(postId, currentUserId, httpRequest);
    }

    public PostResponse createPost(String userId, PostRequest request, HttpServletRequest httpRequest) {
        return postService.createPost(userId, request, httpRequest);
    }

    public PostResponse updatePost(Long postId, String userId, PostRequest request, HttpServletRequest httpRequest) {
        return postService.updatePost(postId, userId, request, httpRequest);
    }

    public void deletePost(Long postId, String userId, HttpServletRequest httpRequest) {
        postService.deletePost(postId, userId, httpRequest);
    }
}