package com.example.post_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostEventRequest {
    private Long eventId;
    private Long postId;
    private String userId;
    private String eventType;
    private String eventData;
    private String previousData;
    private LocalDateTime occurredAt;
}
