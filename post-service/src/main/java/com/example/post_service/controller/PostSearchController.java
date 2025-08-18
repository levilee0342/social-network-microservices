package com.example.post_service.controller;

import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.PostSearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/posts/search")
public class PostSearchController {

    private final PostSearchService postSearchService;

    public PostSearchController(PostSearchService postSearchService) {
        this.postSearchService = postSearchService;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponse>>> searchPosts(
            HttpServletRequest httpRequest,
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset){
        String userId = getCurrentUserId();
        List<PostResponse> posts = postSearchService.searchPostsByQuery(query, userId, limit, offset, httpRequest);
        ApiResponse<List<PostResponse>> apiResponse = ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Posts searched successfully")
                .result(posts)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/search-by-hashtag")
    public ResponseEntity<ApiResponse<List<PostResponse>>> searchByHashtag(
            @RequestParam String hashtag,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        List<PostResponse> posts = postSearchService.searchPostsByHashtag(hashtag, userId, limit, offset, httpRequest);
        return ResponseEntity.ok(ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Posts searched by hashtag successfully")
                .result(posts)
                .build());
    }
}
