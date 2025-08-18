package com.example.post_service.controller;

import com.example.post_service.dto.request.PostShareRequest;
import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.PostShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/shares")
public class PostShareController {

    private final PostShareService postShareService;

    public PostShareController(PostShareService postShareService) {
        this.postShareService = postShareService;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getSharedPosts(
            HttpServletRequest request
    ) {
        String userId = getCurrentUserId();
        List<PostResponse> responses = postShareService.getSharedPostsByUser(userId, request);

        ApiResponse<List<PostResponse>> apiResponse = ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Fetched shared posts successfully")
                .result(responses)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<String>> sharePost(
            @PathVariable Long postId,
            @RequestBody(required = false) PostShareRequest requestBody,
            HttpServletRequest httpRequest
    ) {
        String userId = getCurrentUserId();
        String content = (requestBody != null) ? requestBody.getContent() : null;
        postShareService.sharePost(userId, postId, content, httpRequest);

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .code(200)
                .message("Post shared successfully")
                .result(content)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}
