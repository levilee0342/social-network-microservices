package com.example.notification_service.service.impl;

import com.example.notification_service.dto.request.NotificationRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PostNotificationServiceImpl implements IPostNotificationService {

    private final NotificationRepository notificationRepository;
    private final IRedisPublicProfile redisPublicProfile;
    private final ProducerLog producerLog;
    private final INotificationValidator validator;
    private final IContentGenerator postContentGenerator;
    private final INotificationMapper notificationMapper;

    public PostNotificationServiceImpl(
            NotificationRepository notificationRepository,
            IRedisPublicProfile redisPublicProfile,
            ProducerLog producerLog,
            INotificationValidator validator,
            IContentGenerator postContentGenerator,
            INotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.redisPublicProfile = redisPublicProfile;
        this.producerLog = producerLog;
        this.validator = validator;
        this.postContentGenerator = postContentGenerator;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional
    public Notification createNotification(NotificationRequest request) {
        try {
            String userId = validator.validateUserId(request.getUserId());
            String ownerId = validator.validateUserId(request.getOwnerId());
            var profile = redisPublicProfile.getPublicProfileByUserId(userId);
            if (profile == null) {
                producerLog.sendLog("notification-service", "WARN",
                        "[Profile] Không lấy được profile cho userId: " + userId);
                return null;
            }

            Optional<Notification> existing = notificationRepository
                    .findFirstByUserIdAndTypeAndRelatedEntityIdAndRelatedEntityTypeAndIsReadFalseOrderByCreatedAtDesc(
                            ownerId,
                            NotificationType.INTERACTION,
                            request.getPostId(),
                            "POST"
                    );

            Notification savedNotif = null;

            if (existing.isPresent()) {
                Notification notif = existing.get();
                boolean isRemovalEvent = List.of("UN_LIKE", "DELETE_COMMENT", "DELETE_SHARE")
                        .contains(request.getEventType());

                if (isRemovalEvent) {
                    notif.getActorIds().remove(userId);
                    if (notif.getActorIds().isEmpty()) {
                        notificationRepository.delete(notif);
                        return null;
                    }
                } else {
                    if (!notif.getActorIds().contains(userId)) {
                        notif.getActorIds().add(userId);
                    }
                }

                notif.setContent(postContentGenerator.generateGroupedContent(
                        request.getEventType(), notif.getActorIds()
                ));
                notif.setCreatedAt(LocalDateTime.now());
                savedNotif = notificationRepository.save(notif);

            } else {
                if (!List.of("UN_LIKE", "DELETE_COMMENT", "DELETE_SHARE").contains(request.getEventType())) {
                    Notification newNotif = new Notification();
                    newNotif.setUserId(ownerId);
                    newNotif.setType(NotificationType.INTERACTION);
                    newNotif.setRelatedEntityId(request.getPostId());
                    newNotif.setRelatedEntityType("POST");
                    newNotif.setActorIds(List.of(userId));
                    newNotif.setContent(postContentGenerator.generateGroupedContent(
                            request.getEventType(), List.of(userId)
                    ));
                    savedNotif = notificationRepository.save(newNotif);
                }
            }
            return savedNotif;
        } catch (Exception e) {
            producerLog.sendLog("notification-service", "ERROR",
                    "[Notification] Lỗi khi xử lý sự kiện: " + request + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public Page<NotificationResponse> getNotifications(String userId, boolean onlyUnread, Pageable pageable) {
        try {
            Page<Notification> notifications = onlyUnread ?
                    notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, false, pageable) :
                    notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

            return notifications.map(notificationMapper::mapToNotificationResponse);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Get Notification] Failed to get notification for userId: " + userId + ". Reason: " + e
            );
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
                    "[PostNotification] Empty notificationIds list for userId: " + userId);
            return;
        }
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        for (Notification notification : notifications) {
            if (notification.getUserId().equals(userId) && notification.getRelatedEntityType().equals("POST")) {
                notification.setRead(true);
                try {
                    notificationRepository.save(notification);
                    producerLog.sendLog(
                            "notification-service",
                            "INFO",
                            "[PostNotification] Marked notification " + notification.getId() + " as read for userId: " + userId);
                } catch (Exception e) {
                    producerLog.sendLog(
                            "notification-service",
                            "ERROR",
                            "[PostNotification] Failed to mark notification " + notification.getId() + " as read for userId: " + userId + ": " + e.getMessage());
                }
            } else {
                producerLog.sendLog(
                        "notification-service",
                        "WARN",
                        "[PostNotification] Unauthorized attempt to mark notification " + notification.getId() + " for userId: " + userId);
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
                            "[PostNotification] Notification not found: " + notificationId);
                    return new AppException(NotExistedErrorCode.NOTIFICATION_NOT_FOUND);
                });
        if (!notification.getUserId().equals(userId) || !notification.getRelatedEntityType().equals("POST_REQUEST")) {
            producerLog.sendLog(
                    "notification-service",
                    "WARN",
                    "[PostNotification] Unauthorized delete attempt. NotificationId: " + notificationId + ", Requested by userId: " + userId);
            throw new AppException(AuthErrorCode.NO_PERMISSION_DELETE);
        }

        try {
            notificationRepository.delete(notification);
            producerLog.sendLog(
                    "notification-service",
                    "INFO",
                    "[PostNotification] Deleted notification " + notificationId + " for userId: " + userId);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[PostNotification] Failed to delete notification " + notificationId + " for userId: " + userId + ": " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_TO_DELETE);
        }
    }
}