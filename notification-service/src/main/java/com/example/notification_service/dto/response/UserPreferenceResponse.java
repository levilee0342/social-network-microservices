package com.example.notification_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPreferenceResponse {
    private Long id;
    private String userId;
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean inAppNotifications;
    private LocalDateTime updatedAt;
}
