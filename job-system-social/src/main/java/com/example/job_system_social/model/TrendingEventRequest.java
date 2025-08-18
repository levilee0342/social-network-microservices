package com.example.job_system_social.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingEventRequest {
    private Long postId;
    private String userId;
    private String ownerId;
    private String eventType;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    private String token;
}

