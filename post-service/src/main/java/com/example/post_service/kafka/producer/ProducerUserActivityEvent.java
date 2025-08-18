package com.example.post_service.kafka.producer;

import com.example.post_service.dto.request.PostCreatedRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerUserActivityEvent {

    private final KafkaTemplate<String, PostCreatedRequest> kafkaTemplate;

    public ProducerUserActivityEvent(@Qualifier("postCreatedEventKafkaTemplate") KafkaTemplate<String, PostCreatedRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPostCreatedEvent(PostCreatedRequest event) {
        kafkaTemplate.send("post-created-events", event.getUserId(), event);
        System.out.println("[Kafka] Sent PostCreatedEvent for userId: " + event.getUserId());
    }
}
