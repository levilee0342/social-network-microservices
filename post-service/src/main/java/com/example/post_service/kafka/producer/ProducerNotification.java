package com.example.post_service.kafka.producer;

import com.example.post_service.dto.request.NotificationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerNotification {

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;
    private final ProducerLog logProducer;
    private static final Logger logger = LogManager.getLogger(ProducerNotification.class);

    public ProducerNotification(KafkaTemplate<String, NotificationRequest> kafkaTemplate, ProducerLog logProducer) {
        this.kafkaTemplate = kafkaTemplate;
        this.logProducer = logProducer;
    }

    public void sendNotification(String eventType, String userId, String ownerId, Long postId, String token) {
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setEventType(eventType);
            notification.setUserId(userId);
            notification.setOwnerId(ownerId);
            notification.setPostId(postId);
            notification.setToken(token);
            kafkaTemplate.send("notification-events", userId, notification);
            logger.info("[Producer Notification] Sent notification event to Kafka: {}", notification);
            logProducer.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Producer Notification] Sent notification event to Kafka: " + notification
            );
        } catch (Exception e) {
            logger.error("[Producer Notification] Failed to sent notification event to Kafka: {}", e.getMessage());
            logProducer.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Producer Notification] Failed to sent notification event to Kafka: " + e
            );
        }
    }
}