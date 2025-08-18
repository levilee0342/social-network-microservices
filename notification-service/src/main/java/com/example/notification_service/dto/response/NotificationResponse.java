package com.example.notification_service.dto.response;

import com.example.notification_service.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NotificationResponse {
    private Long id;
    private String userId;
    private NotificationType type;
    private String content;
    private boolean isRead;
    private LocalDateTime createdAt;
    private String relatedEntityId;
    private String relatedEntityType;
    private List<String> actorIds;
}
