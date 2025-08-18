package com.example.post_service.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SearchPostRequest {
    private Long postId;
    private String userId;
    private String title;
    private String content;
    private List<String> hashtags;
}
