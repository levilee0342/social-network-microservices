package com.example.post_service.controller;

import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.service.HashtagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/posts/hashtag")
public class HashtagController {

    private final HashtagService hashtagService;

    public HashtagController(HashtagService hashtagService) {
        this.hashtagService = hashtagService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Set<String>>> searchHashtag(@RequestParam String prefix) {
        Set<String> hashtags = hashtagService.suggestHashtags(prefix);
        return ResponseEntity.ok(ApiResponse.<Set<String>>builder()
                .code(200)
                .message("Filter hashtag successfully")
                .result(hashtags)
                .build());
    }
}
