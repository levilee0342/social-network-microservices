package com.example.notification_service.kafka.consumer;

import com.example.notification_service.controller.WebSocketNotificationController;
import com.example.notification_service.dto.request.FriendNotificationRequest;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.kafka.producer.ProducerLog;
import com.example.notification_service.service.interfaces.IFriendNotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumerFriendNotification {

    private final WebSocketNotificationController webSocketNotificationController;
    private final ProducerLog producerLog;
    private final IFriendNotificationService friendNotificationService;

    public ConsumerFriendNotification(WebSocketNotificationController webSocketNotificationController,
                                      ProducerLog producerLog,
                                      IFriendNotificationService friendNotificationService) {
        this.webSocketNotificationController = webSocketNotificationController;
        this.producerLog = producerLog;
        this.friendNotificationService = friendNotificationService;

    }

    @KafkaListener(topics = "friend-notification-events",
            groupId = "notification-group",
            containerFactory = "friendNotificationKafkaListenerFactory")
    @Transactional
    public void consumeFriendNotificationEvent(FriendNotificationRequest request) {
        try {
            producerLog.sendLog(
                    "notification-service",
                    "DEBUG",
                    "[Kafka Friend Notification] Consume event process friend notification event: " + request);
            Notification notify = friendNotificationService.createFriendNotification(request);
            if (notify != null) {
                // Gửi thông báo với content mới về WebSocket
                webSocketNotificationController.sendFriendNotification(request.getUserId(), notify);
            }
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Kafka Friend Notification] Failed to process friend notification event: " + request + ": " + e.getMessage());
        }
    }
}
