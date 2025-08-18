package com.example.trending_service.kafka.consumer;

import com.example.trending_service.dto.request.TrendingEventRequest;
import com.example.trending_service.kafka.producer.LogProducer;
import com.example.trending_service.service.interfaces.ITrendingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TrendingEventConsumer {

    private final ITrendingService trendingService;
    private final LogProducer logProducer;

    public TrendingEventConsumer(ITrendingService trendingService, LogProducer logProducer) {
        this.trendingService = trendingService;
        this.logProducer = logProducer;
    }

    @KafkaListener(topics = "notification-events", groupId = "trending-group")
    public void listen(TrendingEventRequest notification) {
        try {
            if(notification.getEventType().equals("CREATE_POST")
               || !notification.getUserId().equals(notification.getOwnerId())){
                TrendingEventRequest event = TrendingEventRequest.builder()
                        .postId(notification.getPostId())
                        .eventType(notification.getEventType())
                        .occurredAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .token(notification.getToken())
                        .build();
                trendingService.handleTrendingEvent(event);
            }
        } catch (Exception e) {
            logProducer.sendLog(
                    "trending-service",
                    "ERROR",
                    "[Consumer notification-events] Failed to consume trending event. Reason: " + e);
        }
    }
}

