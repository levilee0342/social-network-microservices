package com.example.post_service.controller;

import com.example.post_service.dto.request.PostRequest;
import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@RequestBody PostRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        PostResponse response = postService.createPost(userId, request, httpRequest);
        ApiResponse<PostResponse> apiResponse = ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Post created successfully")
                .result(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPosts(@RequestParam(required = false) String userId, HttpServletRequest httpRequest) {
        String requestingUserId = getCurrentUserId();
        List<PostResponse> posts = postService.getPosts(requestingUserId, userId, httpRequest);
        ApiResponse<List<PostResponse>> apiResponse = ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Posts retrieved successfully")
                .result(posts)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/new-feed")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPostsNewFeed(
            HttpServletRequest httpRequest) {
        String requestingUserId = getCurrentUserId();
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        List<String> friendIds = postService.getFriendIds(requestingUserId, token);
        List<PostResponse> posts = postService.getNewsfeed(requestingUserId, friendIds, 20, 0);
        ApiResponse<List<PostResponse>> response = ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Posts retrieved successfully")
                .result(posts)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long postId, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        PostResponse response = postService.getPostById(postId, userId, httpRequest);
        return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Post retrieved successfully")
                .result(response)
                .build());
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(@PathVariable Long postId, @RequestBody PostRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        PostResponse response = postService.updatePost(postId, userId, request, httpRequest);
        ApiResponse<PostResponse> apiResponse = ApiResponse.<PostResponse>builder()
                .code(200)
                .message("Post updated successfully")
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        postService.deletePost(postId, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Post deleted successfully")
                .result(null)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}