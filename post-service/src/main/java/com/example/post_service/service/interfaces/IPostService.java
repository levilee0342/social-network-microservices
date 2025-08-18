package com.example.post_service.service.interfaces;

import com.example.post_service.dto.request.PostRequest;
import com.example.post_service.dto.response.PostResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IPostService {
    List<PostResponse> getPosts(String requestingUserId, String targetUserId, HttpServletRequest httpRequest);
    PostResponse getPostById(Long postId, String currentUserId, HttpServletRequest httpRequest);
    PostResponse createPost(String userId, PostRequest request, HttpServletRequest httpRequest);
    PostResponse updatePost(Long postId, String userId, PostRequest request, HttpServletRequest httpRequest);
    void deletePost(Long postId, String userId, HttpServletRequest httpRequest);
}