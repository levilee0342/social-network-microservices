package com.example.user_service.kafka.producer;

import com.example.user_service.dto.request.FriendNotificationRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerNotification {

    private final KafkaTemplate<String, FriendNotificationRequest> kafkaTemplate;
    private final ProducerLog logProducer;
    private static final Logger logger = LogManager.getLogger(ProducerNotification.class);

    public ProducerNotification(final KafkaTemplate<String, FriendNotificationRequest> kafkaTemplate,
                                ProducerLog logProducer){
        this.kafkaTemplate = kafkaTemplate;
        this.logProducer = logProducer;
    }

    public void sendFriendNotification(String eventType, String userId, String relatedUserId, String requestId, String token) {
        try {
            FriendNotificationRequest notification = new FriendNotificationRequest();
            notification.setEventType(eventType);
            notification.setUserId(userId);
            notification.setRelatedUserId(relatedUserId);
            notification.setRequestId(requestId);
            notification.setToken(token);
            kafkaTemplate.send("friend-notification-events", userId, notification);
            logger.info("[Producer Friend Notify] Send friend notification successfully to kafka with request : {}", notification);
        } catch (Exception e) {
            logger.error("[Producer Friend Notify] Failed to send friend notification to kafka: {}", e.getMessage());
            logProducer.sendLog("user-service", "ERROR",
                    "[Producer Friend Notify] Failed to send friend notification for userId: " + userId +
                            ", eventType: " + eventType + ". Reason: " + e.getMessage());
        }
    }
}