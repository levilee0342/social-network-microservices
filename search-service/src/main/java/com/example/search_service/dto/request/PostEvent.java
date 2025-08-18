package com.example.search_service.dto.request;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class PostEvent {
    private String postId;
    private String content;
    private String userId;
    private List<String> hashtags;
    private String visibility;
    private String type; // CREATE, UPDATE, DELETE
    private Long createdAt;
}