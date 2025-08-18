package com.example.post_service.controller;

import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.PostBookMarkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/bookmark")
public class PostBookMarkController {

    private final PostBookMarkService postBookmarkService;

    public PostBookMarkController(PostBookMarkService postBookmarkService) {
        this.postBookmarkService = postBookmarkService;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getSharedPosts(
            HttpServletRequest request
    ) {
        String userId = getCurrentUserId();
        List<PostResponse> responses = postBookmarkService.getBookmarkPostsByUser(userId, request);
        ApiResponse<List<PostResponse>> apiResponse = ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Fetched bookmark posts successfully")
                .result(responses)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> sharePost(
            @PathVariable Long postId,
            HttpServletRequest httpRequest
    ) {
        String userId = getCurrentUserId();
        postBookmarkService.BookmarkPost(userId, postId, httpRequest);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(200)
                .message("Post bookmarked successfully")
                .result(null)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
