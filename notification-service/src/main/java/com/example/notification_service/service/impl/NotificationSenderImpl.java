package com.example.notification_service.service.impl;

import com.example.notification_service.dto.response.ProfileResponse;
import com.example.notification_service.dto.response.PublicProfileResponse;
import com.example.notification_service.dto.response.UserPreferenceResponse;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.kafka.producer.ProducerLog;
import com.example.notification_service.repository.NotificationRepository;
import com.example.notification_service.service.interfaces.INotificationSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationSenderImpl implements INotificationSender {
    private final NotificationRepository notificationRepository;
    private final ProducerLog producerLog;

    public NotificationSenderImpl(NotificationRepository notificationRepository,
                                  ProducerLog producerLog) {
        this.notificationRepository = notificationRepository;
        this.producerLog = producerLog;
    }

    @Override
    @Transactional
    public void sendNotification(String userId, String content, String relatedEntityId, String relatedEntityType,
                                 UserPreferenceResponse preference, PublicProfileResponse profile) {
        if (preference.isInAppNotifications()) {
            saveInAppNotification(userId, content, relatedEntityId, relatedEntityType);
        }
        if (preference.isEmailNotifications() && profile != null) {
            producerLog.sendLog(
                    "notification-service",
                    "INFO",
                    "[Notification] Sending email to: " + profile.getUserId() + ": " + content);
            // TODO: Tích hợp email service
        }
        if (preference.isPushNotifications()) {
            producerLog.sendLog(
                    "notification-service",
                    "INFO",
                    "[Notification] Sending push notification to user: " + userId + ": " + content);
            // TODO: Tích hợp push service
        }
    }

    private void saveInAppNotification(String userId, String content, String relatedEntityId, String relatedEntityType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
//        notification.setType("IN_APP");
        notification.setContent(content);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        try {
            notificationRepository.save(notification);
            producerLog.sendLog(
                    "notification-service",
                    "DEBUG",
                    "[Notification] Saved in-app notification for userId: " + userId);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Notification] Failed to save notification for userId: " + userId + ": " + e.getMessage());
        }
    }
}