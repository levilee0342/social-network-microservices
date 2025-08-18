package com.example.trending_service.controller;

import com.example.trending_service.dto.response.ApiResponse;
import com.example.trending_service.dto.response.PostResponse;
import com.example.trending_service.service.TrendingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trending")
public class TrendingController {

    private final TrendingService trendingService;

    public TrendingController(TrendingService trendingService) {
        this.trendingService = trendingService;
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getTrendingPosts() {
        List<PostResponse> trendingPosts = trendingService.getTrendingPostsFromCache();
        ApiResponse<List<PostResponse>> apiResponse = ApiResponse.<List<PostResponse>>builder()
                .code(200)
                .message("Trending posts retrieved successfully")
                .result(trendingPosts)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
