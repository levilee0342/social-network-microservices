package com.example.notification_service.dto.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private String eventType;
    private String userId;
    private String ownerId;
    private String postId;
    private String token;
}
