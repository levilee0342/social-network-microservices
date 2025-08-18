package com.example.notification_service.service.interfaces;

import com.example.notification_service.dto.response.NotificationResponse;
import com.example.notification_service.entity.Notification;

public interface INotificationMapper {
    NotificationResponse mapToNotificationResponse(Notification notification);
}