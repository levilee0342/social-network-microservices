package com.example.post_service.dto.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private String eventType;
    private String userId;
    private String ownerId;
    private Long postId;
    private String token;
}
