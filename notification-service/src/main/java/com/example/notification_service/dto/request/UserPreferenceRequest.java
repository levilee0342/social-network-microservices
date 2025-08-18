package com.example.notification_service.dto.request;

import lombok.Data;

@Data
public class UserPreferenceRequest {
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean inAppNotifications;
}
