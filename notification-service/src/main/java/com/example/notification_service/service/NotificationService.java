package com.example.notification_service.service;

import com.example.notification_service.dto.request.UserPreferenceRequest;
import com.example.notification_service.dto.response.NotificationResponse;
import com.example.notification_service.dto.response.UserPreferenceResponse;
import com.example.notification_service.service.interfaces.IPostNotificationService;
import com.example.notification_service.service.interfaces.IUserPreferenceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {
    private final IPostNotificationService notificationService;
    private final IUserPreferenceService userPreferenceService;

    public NotificationService(IPostNotificationService notificationService,
                               IUserPreferenceService userPreferenceService) {
        this.notificationService = notificationService;
        this.userPreferenceService = userPreferenceService;
    }

    public Page<NotificationResponse> getNotifications(String userId, boolean onlyUnread, Pageable pageable) {
        return notificationService.getNotifications(userId, onlyUnread, pageable);
    }

    public void markAsRead(String userId, List<Long> notificationIds) {
        notificationService.markAsRead(userId, notificationIds);
    }

    public void deleteNotification(String userId, Long notificationId) {
        notificationService.deleteNotification(userId, notificationId);
    }

    public UserPreferenceResponse updatePreferences(String userId, UserPreferenceRequest request) {
        return userPreferenceService.updatePreferences(userId, request);
    }

    public UserPreferenceResponse getPreferences(String userId) {
        return userPreferenceService.getPreferences(userId);
    }
}