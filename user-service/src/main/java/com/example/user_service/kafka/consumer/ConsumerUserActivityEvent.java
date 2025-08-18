package com.example.user_service.kafka.consumer;

import com.example.user_service.dto.request.PostCreatedRequest;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.service.interfaces.IUserDailyActivityService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerUserActivityEvent {

    private final IUserDailyActivityService activityService;
    private final ProducerLog logProducer;
    private static final Logger logger = LogManager.getLogger(ConsumerUserActivityEvent.class);

    public ConsumerUserActivityEvent(IUserDailyActivityService activityService,
                                     ProducerLog logProducer) {
        this.activityService = activityService;
        this.logProducer = logProducer;
    }

    @KafkaListener(topics = "post-created-events", groupId = "post-created-group", containerFactory = "postCreatedEventKafkaListenerContainerFactory")
    public void consumePostCreatedEvent(PostCreatedRequest event) {
        try {
            activityService.recordActivity(event.getUserId());
            logger.info("[Consumer User Activity] Event received from kafka with request : {}", event);
        } catch (Exception e) {
            logger.error("[Consumer User Activity] Failed to send event to kafka : {}", e.getMessage());
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[UserActivity] Failed to record activity for user : " + event.getUserId() + ". Reason: " + e);
        }
    }
}
