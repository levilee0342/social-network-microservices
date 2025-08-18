package com.example.post_service.kafka.producer;

import com.example.post_service.dto.request.PostEventRequest;
import com.example.post_service.entity.PostEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProducerEvent {

    private final KafkaTemplate<String, PostEventRequest> kafkaTemplate;
    private final ProducerLog logProducer;
    private static final Logger logger = LogManager.getLogger(ProducerEvent.class);

    public ProducerEvent(KafkaTemplate<String, PostEventRequest> kafkaTemplate, ProducerLog logProducer) {
        this.kafkaTemplate = kafkaTemplate;
        this.logProducer = logProducer;
    }

    public void publishToKafka(PostEvent event) {
        try {
            PostEventRequest eventRequest = mapToEventRequest(event);
            kafkaTemplate.send("post-events", event.getPostId().toString(), eventRequest);
            logger.info("[Producer Event] Sent event to kafka : {}", event);
            logProducer.sendLog(
                    "post-service",
                    "DEBUG",
                    "[Producer Event] Sent event to kafka :" + event
            );
        } catch (Exception e) {
            logger.error("[Producer Event] Failed to send event to kafka : {}", e.getMessage());
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Producer Event] Failed to send event to kafka :" + event + "\n" + e.getMessage()
            );
        }
    }

    private PostEventRequest mapToEventRequest(PostEvent event) {
        PostEventRequest request = new PostEventRequest();
        request.setEventId(event.getEventId());
        request.setPostId(event.getPostId());
        request.setUserId(event.getUserId());
        request.setEventType(event.getEventType().name());
        request.setEventData(event.getEventData());
        request.setPreviousData(event.getPreviousData());
        request.setOccurredAt(event.getOccurredAt());
        return request;
    }
}