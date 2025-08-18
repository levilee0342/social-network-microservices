package com.example.trending_service.dto.request;

import lombok.*;

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

