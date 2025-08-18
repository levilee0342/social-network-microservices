package com.example.presence_service.kafka.producer;

import com.example.presence_service.entity.PresenceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PresenceEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(PresenceEventPublisher.class);
    private static final String TOPIC = "presence-events";
    private final KafkaTemplate<String, PresenceEvent> kafkaTemplate;

    public PresenceEventPublisher(KafkaTemplate<String, PresenceEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEvent(PresenceEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.getUserId(), event);
            logger.info("Published presence event for userId: {}", event.getUserId());
        } catch (Exception e) {
            logger.error("Failed to publish presence event for userId: {}, error: {}", event.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to publish presence event", e);
        }
    }
}