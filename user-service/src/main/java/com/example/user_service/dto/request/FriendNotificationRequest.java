package com.example.user_service.dto.request;

import lombok.Data;

@Data
public class FriendNotificationRequest {
    private String eventType;
    private String userId;
    private String relatedUserId;
    private String requestId;
    private String token;
}
