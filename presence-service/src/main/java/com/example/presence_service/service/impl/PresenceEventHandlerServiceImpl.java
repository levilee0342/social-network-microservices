package com.example.presence_service.service.impl;

import com.example.presence_service.entity.PresenceEvent;
import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.entity.UserPresence;
import com.example.presence_service.error.AuthErrorCode;
import com.example.presence_service.exception.AppException;
import com.example.presence_service.kafka.producer.LogProducer;
import com.example.presence_service.scheduler.WebSocketHeartbeatScheduler;
import com.example.presence_service.service.interfaces.IPresenceEventHandlerService;
import com.example.presence_service.service.interfaces.IPresenceService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PresenceEventHandlerServiceImpl implements IPresenceEventHandlerService {

    private final IPresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketHeartbeatScheduler heartbeatScheduler;
    private final KafkaTemplate<String, PresenceEvent> kafkaTemplate;
    private final LogProducer logProducer;

    public PresenceEventHandlerServiceImpl(
            IPresenceService presenceService,
            SimpMessagingTemplate messagingTemplate,
            WebSocketHeartbeatScheduler heartbeatScheduler,
            KafkaTemplate<String, PresenceEvent> kafkaTemplate,
            LogProducer logProducer) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
        this.heartbeatScheduler = heartbeatScheduler;
        this.kafkaTemplate = kafkaTemplate;
        this.logProducer = logProducer;
    }

    @Override
    public void handleEvent(PresenceEvent event) {
        logProducer.sendLog(
                "presence-service",
                "INFO",
                "Handling presence event: userId=" + event.getUserId() + ", eventType=" + event.getEventType());
        try {
            if (event.getEventType() == PresenceEventType.PONG) {
                heartbeatScheduler.recordPongResponse(event.getUserId());
                UserPresence presence = presenceService.getPresence(event.getUserId());
                if (presence != null && presence.getStatus() == PresenceEventType.IDLE) {
                    presenceService.updatePresence(event.getUserId(), PresenceEventType.ONLINE, presence.getRoomId());
                    PresenceEvent onlineEvent = createPresenceEvent(event.getUserId(), PresenceEventType.ONLINE, presence.getRoomId());
                    messagingTemplate.convertAndSend("/topic/presence", onlineEvent);
                    kafkaTemplate.send("presence-events", event.getUserId(), onlineEvent);
                    logProducer.sendLog(
                            "presence-service",
                            "DEBUG",
                            "User " + event.getUserId() + " returned to ONLINE from IDLE");
                }
                return;
            }

            presenceService.updatePresence(event.getUserId(), event.getEventType(), event.getRoomId());
            kafkaTemplate.send("presence-events", event.getUserId(), event);
            switch (event.getEventType()) {
                case TYPING:
                    if (event.getRoomId() != null) {
                        messagingTemplate.convertAndSend("/topic/presence/" + event.getRoomId(), event);
                    }
                    break;
                case ONLINE:
                case OFFLINE:
                case IDLE:
                case AWAY:
                    messagingTemplate.convertAndSend("/topic/presence", event);
                    if (event.getRoomId() != null) {
                        messagingTemplate.convertAndSend("/topic/presence/room/" + event.getRoomId(), event);
                    }
                    break;
                default:
                    logProducer.sendLog(
                            "presence-service"  ,
                            "WARN",
                            "Unhandled presence event type: " + event.getEventType());
            }
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "Processed presence event: userId=" + event.getUserId() + ", eventType=" + event.getEventType() + ", roomId=" + event.getRoomId());
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Error handling presence event for user: " + event.getUserId() + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_HANDING_EVENT);
        }
    }

    private PresenceEvent createPresenceEvent(String userId, PresenceEventType eventType, String roomId) {
        PresenceEvent event = new PresenceEvent();
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setRoomId(roomId);
        event.setTimestamp(System.currentTimeMillis());
        return event;
    }
}