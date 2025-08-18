package com.example.notification_service.service.impl;

import com.example.notification_service.controller.WebSocketNotificationController;
import com.example.notification_service.dto.request.FriendNotificationRequest;
import com.example.notification_service.dto.response.NotificationResponse;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.enums.NotificationType;
import com.example.notification_service.error.AuthErrorCode;
import com.example.notification_service.error.NotExistedErrorCode;
import com.example.notification_service.exception.AppException;
import com.example.notification_service.kafka.producer.ProducerLog;
import com.example.notification_service.repository.NotificationRepository;
import com.example.notification_service.repository.redis.IRedisPublicProfile;
import com.example.notification_service.service.interfaces.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FriendNotificationServiceImpl implements IFriendNotificationService {
    private final NotificationRepository notificationRepository;
    private final IRedisPublicProfile redisPublicProfile;
    private final ProducerLog producerLog;
    private final INotificationValidator validator;
    private final IFriendContentGenerator friendContentGenerator;
    private final INotificationMapper notificationMapper;

    public FriendNotificationServiceImpl(
            NotificationRepository notificationRepository,
            IRedisPublicProfile redisPublicProfile,
            ProducerLog producerLog,
            INotificationValidator validator,
            IFriendContentGenerator friendContentGenerator,
            INotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.redisPublicProfile = redisPublicProfile;
        this.producerLog = producerLog;
        this.validator = validator;
        this.friendContentGenerator = friendContentGenerator;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional
    public Notification createFriendNotification(FriendNotificationRequest request) {
        try {
            String userId = validator.validateUserId(request.getUserId());
            var profile = redisPublicProfile.getPublicProfileByUserId(request.getRelatedUserId());
            if (profile == null) {
                producerLog.sendLog(
                        "notification-service",
                        "WARN",
                        "[Profile] Profile is not found with userId: " + request.getRelatedUserId());
            }
            // Lưu thông báo
            Notification newNotif = new Notification();
            newNotif.setUserId(request.getUserId());
            newNotif.setType(NotificationType.FRIEND);
            newNotif.setRelatedEntityId(request.getRelatedUserId());
            newNotif.setRelatedEntityType("FRIEND");
            newNotif.setActorIds(List.of(userId));
            newNotif.setContent(friendContentGenerator.generateFriendContent(request.getEventType(), profile));
            return notificationRepository.save(newNotif);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Create Friend Notification] Failed to create notification for userId: " + request.getRelatedUserId() + ". Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_SAVE_NOTIFICATION);
        }
    }

    @Override
    public Page<NotificationResponse> getNotifications(String userId, boolean onlyUnread, Pageable pageable) {
        try {
            Page<Notification> notifications = onlyUnread ?
                    notificationRepository.findByUserIdAndIsReadAndRelatedEntityType(userId, false, "FRIEND_REQUEST", pageable) :
                    notificationRepository.findByUserIdAndRelatedEntityType(userId, "FRIEND_REQUEST", pageable);
            return notifications.map(notificationMapper::mapToNotificationResponse);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Get Notification] Failed to get notification for userId: " +userId + ". Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_GET_NOTIFICATION);
        }
    }

    @Override
    @Transactional
    public void markAsRead(String userId, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            producerLog.sendLog(
                    "notification-service",
                    "WARN",
                    "[FriendNotification] Empty notificationIds list for userId: " + userId);
            return;
        }
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        for (Notification notification : notifications) {
            if (notification.getUserId().equals(userId) && notification.getRelatedEntityType().equals("FRIEND_REQUEST")) {
                notification.setRead(true);
                try {
                    notificationRepository.save(notification);
                    producerLog.sendLog(
                            "notification-service",
                            "DEBUG",
                            "[FriendNotification] Marked notification " + notification.getId() + " as read for userId: " + userId);
                } catch (Exception e) {
                    producerLog.sendLog(
                            "notification-service",
                            "ERROR",
                            "[FriendNotification] Failed to mark notification " + notification.getId() + " as read for userId: " + userId + ": " + e.getMessage());
                }
            } else {
                producerLog.sendLog(
                        "notification-service",
                        "WARN",
                        "[FriendNotification] Unauthorized attempt to mark notification " + notification.getId() + " for userId: " + userId);
            }
        }
    }

    @Override
    @Transactional
    public void deleteNotification(String userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    producerLog.sendLog(
                            "notification-service",
                            "ERROR",
                            "[FriendNotification] Notification not found: " + notificationId);
                    return new AppException(NotExistedErrorCode.NOTIFICATION_NOT_FOUND);
                });

        if (!notification.getUserId().equals(userId) || !notification.getRelatedEntityType().equals("FRIEND_REQUEST")) {
            producerLog.sendLog(
                    "notification-service",
                    "WARN",
                    "[FriendNotification] Unauthorized delete attempt. NotificationId: " + notificationId + ", Requested by userId: " + userId);
            throw new AppException(AuthErrorCode.NO_PERMISSION_DELETE);
        }

        try {
            notificationRepository.delete(notification);
            producerLog.sendLog(
                    "notification-service",
                    "DEBUG",
                    "[FriendNotification] Deleted notification " + notificationId + " for userId: " + userId);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[FriendNotification] Failed to delete notification " + notificationId + " for userId: " + userId + ": " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_TO_DELETE);
        }
    }
}