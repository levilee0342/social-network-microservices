package com.example.presence_service.scheduler;

import com.example.presence_service.entity.PresenceEvent;
import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.entity.UserPresence;
import com.example.presence_service.service.interfaces.IPresenceService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHeartbeatScheduler {
    private static final long PONG_TIMEOUT_MS = 10_000; // 10 seconds
    private static final long IDLE_THRESHOLD_MS = 60_000; // 1 minute
    private static final long AWAY_THRESHOLD_MS = 5 * 60_000; // 5 minutes
    private static final long FORCE_OFFLINE_THRESHOLD_MS = 7 * 60_000; // 7 minutes

    private final KafkaTemplate<String, PresenceEvent> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final IPresenceService presenceService;
    private final Map<String, Long> lastPingSent = new ConcurrentHashMap<>();
    private final Map<String, Long> lastClientActivity = new ConcurrentHashMap<>();
    private final Map<String, Long> potentialOfflineUsers = new ConcurrentHashMap<>();
    private final Set<String> connectedUserIds = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, Long> lastPongResponse = new ConcurrentHashMap<>();

    public WebSocketHeartbeatScheduler(SimpMessagingTemplate messagingTemplate,
                                       IPresenceService presenceService,
                                       KafkaTemplate<String, PresenceEvent> kafkaTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.presenceService = presenceService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void addConnectedUser(String userId) {
        long now = System.currentTimeMillis();
        connectedUserIds.add(userId);
        lastPingSent.put(userId, now);
        lastClientActivity.put(userId, now);
        potentialOfflineUsers.remove(userId);
        System.out.println("Added connected user: " + userId);
    }

    public void removeConnectedUser(String userId) {
        connectedUserIds.remove(userId);
        lastPingSent.remove(userId);
        lastClientActivity.remove(userId);
        potentialOfflineUsers.remove(userId);
        System.out.println("Removed disconnected user: " + userId);
    }

    public void recordClientActivity(String userId) {
        if (userId != null) {
            lastClientActivity.put(userId, System.currentTimeMillis());
            potentialOfflineUsers.remove(userId);
            System.out.println("Recorded activity from user: " + userId);
        }
    }

    public void recordPongResponse(String userId) {
        if (userId != null) {
            lastPongResponse.put(userId, System.currentTimeMillis());
            recordClientActivity(userId);
            UserPresence presence = presenceService.getPresence(userId);
            if (presence != null && (presence.getStatus() == PresenceEventType.IDLE || presence.getStatus() == PresenceEventType.AWAY)) {
                updatePresence(userId, PresenceEventType.ONLINE, presence.getRoomId());
                System.out.println("User " + userId + " returned to ONLINE from " + presence.getStatus());
            }
            System.out.println("Received PONG from user: " + userId);
        }
    }

    @Scheduled(fixedRate = 30_000)
    public void sendHeartbeatPings() {
        if (connectedUserIds.isEmpty()) {
            System.out.println("No connected users to send heartbeats to.");
            return;
        }

        long now = System.currentTimeMillis();
        System.out.println("Sending heartbeat PINGs to " + connectedUserIds.size() + " connected users.");

        for (String userId : connectedUserIds) {
            PresenceEvent pingEvent = createPresenceEvent(userId, PresenceEventType.PING, null);
            messagingTemplate.convertAndSend("/topic/presence/" + userId + "/ping", pingEvent);
            lastPingSent.put(userId, now);
            System.out.println("Sent PING to user: " + userId);
        }
    }

    @Scheduled(fixedDelay = 15_000)
    public void checkStaleConnectionsAndIdleStatus() {
        long now = System.currentTimeMillis();

        for (String userId : connectedUserIds) {
            try {
                UserPresence currentPresence = presenceService.getPresence(userId);
                if (currentPresence == null) {
                    System.out.println("No presence found for connected user: " + userId);
                    continue;
                }

                Long lastActivity = lastClientActivity.getOrDefault(userId, currentPresence.getLastActive());
                if (lastActivity == 0) {
                    lastActivity = now;
                    lastClientActivity.put(userId, now);
                }

                // Check Heartbeat (PING/PONG)
                Long lastPing = lastPingSent.get(userId);
                Long lastPong = lastPongResponse.get(userId);

                boolean pongTooOld = (lastPong == null || lastPing - lastPong > PONG_TIMEOUT_MS);

                if (pongTooOld) {
                    Long firstSuspectTime = potentialOfflineUsers.get(userId);

                    if (firstSuspectTime == null) {
                        // Lần đầu không phản hồi, đánh dấu nghi ngờ
                        potentialOfflineUsers.put(userId, now);
                        System.out.println("User " + userId + " did not respond to PING. Marked as potential offline.");
                    } else {
                        long offlineDuration = now - firstSuspectTime;
                        System.out.println("User " + userId + " has been potentially offline for " + offlineDuration + " ms.");

                        if (offlineDuration > FORCE_OFFLINE_THRESHOLD_MS) {
                            System.out.println("User " + userId + " has not responded for too long. Forcing OFFLINE.");

                            // Xử lý buộc OFFLINE
                            removeConnectedUser(userId);
                            presenceService.updatePresence(userId, PresenceEventType.OFFLINE, null);

                            PresenceEvent presenceEvent = createPresenceEvent(userId, PresenceEventType.OFFLINE, null);
                            messagingTemplate.convertAndSend("/topic/presence", presenceEvent);
                            //Send to Kafka
                            kafkaTemplate.send("presence-events", userId, presenceEvent);
                            // Cleanup sau khi OFFLINE
                            potentialOfflineUsers.remove(userId);
                        }
                    }
                } else {
                    // Nếu đã nhận PONG hợp lệ thì xóa khỏi danh sách nghi ngờ
                    potentialOfflineUsers.remove(userId);
                }

                // Check IDLE/AWAY status
                long timeSinceLastActivity = now - lastActivity;
                if (isOnlineOrTyping(currentPresence) && timeSinceLastActivity > IDLE_THRESHOLD_MS) {
                    updatePresence(userId, PresenceEventType.IDLE, currentPresence.getRoomId());
                    System.out.println("User " + userId + " is IDLE.");
                } else if (currentPresence.getStatus() == PresenceEventType.IDLE && timeSinceLastActivity > AWAY_THRESHOLD_MS) {
                    updatePresence(userId, PresenceEventType.AWAY, currentPresence.getRoomId());
                    System.out.println("User " + userId + " is AWAY.");
                }
            } catch (Exception e) {
                System.out.println("Error processing presence for user: " + userId + ", error: " + e.getMessage());
            }
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

    private boolean isOnlineOrTyping(UserPresence presence) {
        return presence.getStatus() == PresenceEventType.ONLINE || presence.getStatus() == PresenceEventType.TYPING;
    }

    private void updatePresence(String userId, PresenceEventType eventType, String roomId) {
        presenceService.updatePresence(userId, eventType, roomId);
        PresenceEvent presenceEvent = createPresenceEvent(userId, eventType, roomId);
        messagingTemplate.convertAndSend("/topic/presence", presenceEvent);
        kafkaTemplate.send("presence-events", userId, presenceEvent);
    }
}