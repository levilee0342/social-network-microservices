package com.example.post_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCreatedRequest {
    private String userId;
    private Long postId;
    private Long createdAt;
}
