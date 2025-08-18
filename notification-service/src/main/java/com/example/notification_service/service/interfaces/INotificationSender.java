package com.example.notification_service.service.interfaces;

import com.example.notification_service.dto.response.PublicProfileResponse;
import com.example.notification_service.dto.response.UserPreferenceResponse;

public interface INotificationSender {
    void sendNotification(String userId, String content, String relatedEntityId, String relatedEntityType,
                          UserPreferenceResponse preference, PublicProfileResponse profile);
}