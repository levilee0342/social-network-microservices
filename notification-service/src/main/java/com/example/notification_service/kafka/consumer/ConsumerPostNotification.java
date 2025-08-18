package com.example.notification_service.kafka.consumer;

import com.example.notification_service.controller.WebSocketNotificationController;
import com.example.notification_service.dto.request.NotificationRequest;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.kafka.producer.ProducerLog;
import com.example.notification_service.service.interfaces.IPostNotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsumerPostNotification {

    private final WebSocketNotificationController webSocketNotificationController;
    private final IPostNotificationService postNotificationService;
    private final ProducerLog producerLog;

    public ConsumerPostNotification(WebSocketNotificationController webSocketNotificationController,
                                    IPostNotificationService postNotificationService,
                                    ProducerLog producerLog) {
        this.postNotificationService = postNotificationService;
        this.producerLog = producerLog;
        this.webSocketNotificationController = webSocketNotificationController;
    }

    @KafkaListener(
            topics = "notification-events",
            groupId = "notification-group",
            containerFactory = "notificationKafkaListenerFactory")
    @Transactional
    public void consumeNotificationEvent(NotificationRequest request) {
        try {
            producerLog.sendLog(
                    "notification-service",
                    "DEBUG",
                    "[PostNotification] Consume event process posts notification event: " + request);
            Notification notify = postNotificationService.createNotification(request);
            if (notify != null && !request.getEventType().equals("CREATE_POST")) {
                // Gửi thông báo với content mới về WebSocket
                webSocketNotificationController.sendPostNotification(request.getOwnerId(), notify);
            }
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[PostNotification] Failed to process notification event: " + request + ": " + e.getMessage());
        }
    }

}
