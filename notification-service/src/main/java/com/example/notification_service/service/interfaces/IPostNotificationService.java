package com.example.notification_service.service.interfaces;

import com.example.notification_service.dto.request.NotificationRequest;
import com.example.notification_service.dto.response.NotificationResponse;
import com.example.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPostNotificationService {
    Notification createNotification(NotificationRequest request);
    Page<NotificationResponse> getNotifications(String userId, boolean onlyUnread, Pageable pageable);
    void markAsRead(String userId, List<Long> notificationIds);
    void deleteNotification(String userId, Long notificationId);
}