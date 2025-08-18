package com.example.presence_service.kafka.consumer;

import com.example.presence_service.entity.EventPresence;
import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.kafka.producer.LogProducer;
import com.example.presence_service.scheduler.WebSocketHeartbeatScheduler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerEventPresence {

    private final WebSocketHeartbeatScheduler webSocketHeartbeatScheduler;
    private final LogProducer logProducer;

    public ConsumerEventPresence(WebSocketHeartbeatScheduler webSocketHeartbeatScheduler, LogProducer logProducer) {
        this.webSocketHeartbeatScheduler = webSocketHeartbeatScheduler;
        this.logProducer = logProducer;
    }

    @KafkaListener(topics = "user-presence-topic", groupId = "presence-group")
    public void handleLoginPresenceEvent(EventPresence event) {
        try {
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "[Consumer user-presence-topic] Successfully to receive presence event: " + event);
            if(event.getEventType() == PresenceEventType.ONLINE) {
                webSocketHeartbeatScheduler.addConnectedUser(event.getUserId());
            }
            if(event.getEventType() == PresenceEventType.OFFLINE) {
                webSocketHeartbeatScheduler.removeConnectedUser(event.getUserId());
            }
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "[Consumer user-presence-topic] Failed to receive presence event: " + event);
        }
    }

}
