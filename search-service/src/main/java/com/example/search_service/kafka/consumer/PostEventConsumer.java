package com.example.search_service.kafka.consumer;

import com.example.search_service.dto.request.PostEvent;
import com.example.search_service.service.interfaces.IPostEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PostEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(PostEventConsumer.class);
    private final IPostEventHandler postEventHandler;

    public PostEventConsumer(IPostEventHandler postEventHandler) {
        this.postEventHandler = postEventHandler;
    }

    @KafkaListener(topics = "post-events", groupId = "search-service")
    public void handlePostEvent(PostEvent event) {
        if (event == null || event.getPostId() == null || event.getType() == null) {
            logger.error("Invalid PostEvent: {}", event);
            return;
        }

        try {
            postEventHandler.handle(event);
            logger.info("Processed PostEvent: {}", event.getPostId());
        } catch (Exception e) {
            logger.error("Error processing PostEvent: {}", event, e);
            // TODO: Send to Dead Letter Queue
        }
    }
}
