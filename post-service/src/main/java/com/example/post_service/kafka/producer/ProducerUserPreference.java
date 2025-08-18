package com.example.post_service.kafka.producer;

import com.example.post_service.dto.request.UserInteractionEventRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerUserPreference {

    private final KafkaTemplate<String, UserInteractionEventRequest> kafkaTemplate;
    private final ProducerLog producerLog;

    public ProducerUserPreference(@Qualifier("userPreferenceEventKafkaTemplate") KafkaTemplate<String, UserInteractionEventRequest> kafkaTemplate,
                                  ProducerLog producerLog) {
        this.kafkaTemplate = kafkaTemplate;
        this.producerLog = producerLog;
    }

    public void publisherUserPreferenceEvent(UserInteractionEventRequest userInteractionEventRequest) {
        try {
            kafkaTemplate.send("user-interactions", userInteractionEventRequest);
            producerLog.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Kafka topic user-interactions] User interaction event sent " + userInteractionEventRequest
            );
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Kafka topic user-interactions] Failed to user interaction event sent " + userInteractionEventRequest + ". Reason: " + e
            );
        }
    }
}
