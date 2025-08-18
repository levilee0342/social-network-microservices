package com.example.post_service.dto.request;

import lombok.Data;

@Data
public class CommentRequest {
    Long postId;
    String userId;
    String content;
    String imageUrl;
}
