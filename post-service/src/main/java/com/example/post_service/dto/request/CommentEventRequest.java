package com.example.post_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentEventRequest {
    private String eventType;
    private String userId;
    private Long postId;
    private String content;
    private Long timestamp;
}

