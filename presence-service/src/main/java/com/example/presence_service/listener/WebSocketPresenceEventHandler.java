package com.example.presence_service.listener;

import com.example.presence_service.entity.PresenceEvent;
import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.entity.UserPresence;
import com.example.presence_service.error.AuthErrorCode;
import com.example.presence_service.exception.AppException;
import com.example.presence_service.kafka.producer.LogProducer;
import com.example.presence_service.scheduler.WebSocketHeartbeatScheduler;
import com.example.presence_service.service.interfaces.IPresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketPresenceEventHandler {

    private final IPresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketHeartbeatScheduler heartbeatScheduler;
    private final LogProducer logProducer;

    public WebSocketPresenceEventHandler(
            IPresenceService presenceService,
            SimpMessagingTemplate messagingTemplate,
            WebSocketHeartbeatScheduler heartbeatScheduler,
            LogProducer logProducer) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
        this.heartbeatScheduler = heartbeatScheduler;
        this.logProducer = logProducer;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "WebSocket connection attempt without principal");
            throw new AppException(AuthErrorCode.WEBSOCKET_NOT_PRINCIPAL);
        }
        String userId = principal.getName();
        try {
            presenceService.updatePresence(userId, PresenceEventType.ONLINE, null);
            heartbeatScheduler.addConnectedUser(userId);
            PresenceEvent onlineEvent = createPresenceEvent(userId, PresenceEventType.ONLINE, null);
            messagingTemplate.convertAndSend("/topic/presence", onlineEvent);
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "User connected: " + userId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to update presence on connect for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PRESENCE);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        if (principal == null) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "WebSocket disconnection attempt without principal");
            throw new AppException(AuthErrorCode.WEBSOCKET_NOT_PRINCIPAL);
        }
        String userId = principal.getName();
        try {
            String lastRoomId = getLastRoomId(userId);
            presenceService.updatePresence(userId, PresenceEventType.OFFLINE, null);
            heartbeatScheduler.removeConnectedUser(userId);
            PresenceEvent offlineEvent = createPresenceEvent(userId, PresenceEventType.OFFLINE, lastRoomId);
            messagingTemplate.convertAndSend("/topic/presence", offlineEvent);
            if (lastRoomId != null && !lastRoomId.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/presence/room/" + lastRoomId, offlineEvent);
            }
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "User disconnected: " + userId);
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to update presence on disconnect for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PRESENCE);
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

    private String getLastRoomId(String userId) {
        try {
            UserPresence presence = presenceService.getPresence(userId);
            return presence != null ? presence.getRoomId() : null;
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to get last room ID for user: " + userId + ", error: " + e.getMessage());
            return null;
        }
    }
}