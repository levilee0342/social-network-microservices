package com.example.post_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostAuditResponse {
    private Long eventId;
    private Long postId;
    private String userId;
    private String eventType;
    private String eventData;
    private String previousData;
    private String ipAddress;
    private String userAgent;
    private String reason;
    private LocalDateTime occurredAt;
}
