package com.example.job_system_social.model;

import lombok.Data;

@Data
public class NotificationRequest {
    private String eventType;
    private String userId;
    private String ownerId;
    private Long postId;
    private String token;
}
