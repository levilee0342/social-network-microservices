package com.example.presence_service.service.impl;

import com.example.presence_service.entity.PresenceEvent;
import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.entity.UserPresence;
import com.example.presence_service.error.AuthErrorCode;
import com.example.presence_service.exception.AppException;
import com.example.presence_service.kafka.producer.LogProducer;
import com.example.presence_service.kafka.producer.PresenceEventPublisher;
import com.example.presence_service.repository.ICouchbaseUserPresenceRepository;
import com.example.presence_service.repository.IRedisUserPresenceRepository;
import com.example.presence_service.service.interfaces.IPresenceService;
import com.example.presence_service.service.interfaces.IPresenceTtlStrategyService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class PresenceServiceImpl implements IPresenceService {

    private static final long ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L; // 1 week in milliseconds
    private final IRedisUserPresenceRepository presenceRedisRepository;
    private final PresenceEventPublisher eventPublisher;
    private final ICouchbaseUserPresenceRepository presenceCouchbaseRepository;
    private final IPresenceTtlStrategyService ttlStrategy;
    private final LogProducer logProducer;

    public PresenceServiceImpl(
            IRedisUserPresenceRepository presenceRedisRepository,
            PresenceEventPublisher eventPublisher,
            ICouchbaseUserPresenceRepository presenceCouchbaseRepository,
            IPresenceTtlStrategyService ttlStrategy,
            LogProducer logProducer) {
        this.presenceRedisRepository = presenceRedisRepository;
        this.eventPublisher = eventPublisher;
        this.presenceCouchbaseRepository = presenceCouchbaseRepository;
        this.ttlStrategy = ttlStrategy;
        this.logProducer = logProducer;
    }

    @Override
    public void updatePresence(String userId, PresenceEventType status, String roomId) {
        try {
            long now = System.currentTimeMillis();
            UserPresence existing = presenceRedisRepository.findByUserId(userId);
            if (existing != null && isPresenceUnchanged(existing, status, roomId)) {
                logProducer.sendLog(
                        "presence-service",
                        "INFO",
                        "Presence unchanged for user: " + userId + ", status: " + status);
                return;
            }
            UserPresence presence = buildPresence(userId, status, roomId, now, existing);
            savePresence(presence);
            publishPresenceEventAsync(userId, status, roomId, now);
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "Updated presence for user: " + userId + ", status: " + status);
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to update presence for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PRESENCE);
        }
    }

    @Override
    public void setAwayStatus(String userId, String roomId) {
        try {
            updatePresence(userId, PresenceEventType.AWAY, roomId);
            logProducer.sendLog(
                    "presence-service",
                    "INFO",
                    "User " + userId + " set to AWAY status");
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to set AWAY status for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PRESENCE);
        }
    }

    @Override
    public UserPresence getPresence(String userId) {
        try {
            UserPresence presence = presenceRedisRepository.findByUserId(userId);
            if (presence != null) {
                if (presence.getStatus() == PresenceEventType.OFFLINE &&
                        System.currentTimeMillis() - presence.getLastActive() > ONE_WEEK_MS) {
                    presence.setStatus(PresenceEventType.UNKNOWN);
                    logProducer.sendLog(
                            "presence-service",
                            "INFO",
                            "User " + userId + " marked as UNKNOWN due to long offline duration");
                }
                logProducer.sendLog(
                        "presence-service",
                        "INFO",
                        "Retrieved presence from Redis for user: " + userId);
                return presence;
            }
            presence = presenceCouchbaseRepository.findByUserId(userId);
            if (presence != null) {
                if (presence.getStatus() == PresenceEventType.OFFLINE &&
                        System.currentTimeMillis() - presence.getLastActive() > ONE_WEEK_MS) {
                    presence.setStatus(PresenceEventType.UNKNOWN);
                    logProducer.sendLog(
                            "presence-service",
                            "INFO",
                            "User " + userId + " marked as UNKNOWN due to long offline duration");
                }
                Duration ttl = ttlStrategy.getTtl(presence.getStatus());
                if (!ttl.isZero()) {
                    presenceRedisRepository.saveWithTTL(presence, ttl);
                }
                logProducer.sendLog(
                        "presence-service",
                        "DEBUG",
                        "Retrieved presence from Couchbase for user: " + userId);
                return presence;
            }
            UserPresence defaultPresence = createDefaultPresence(userId);
            savePresence(defaultPresence);
            logProducer.sendLog(
                    "presence-service",
                    "DEBUG",
                    "Created default presence for user: " + userId);
            return defaultPresence;
        } catch (Exception e) {
            logProducer.sendLog(
                    "presence-service",
                    "ERROR",
                    "Failed to get presence for user: " + userId + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_PRESENCE);
        }
    }

    private boolean isPresenceUnchanged(UserPresence existing, PresenceEventType status, String roomId) {
        return existing.getStatus() == status &&
                Objects.equals(existing.getRoomId(), roomId);
    }

    private UserPresence buildPresence(String userId, PresenceEventType status, String roomId, long now, UserPresence existing) {
        UserPresence presence = new UserPresence();
        presence.setUserId(userId);
        presence.setStatus(status);
        presence.setRoomId(roomId);
        presence.setLastActive(now);
        presence.setLastOnlineTimestamp(status == PresenceEventType.ONLINE
                ? now
                : (existing != null ? existing.getLastOnlineTimestamp() : 0));
        return presence;
    }

    private UserPresence createDefaultPresence(String userId) {
        long now = System.currentTimeMillis();
        UserPresence presence = new UserPresence();
        presence.setUserId(userId);
        presence.setStatus(PresenceEventType.ONLINE);
        presence.setLastActive(now);
        presence.setLastOnlineTimestamp(now);
        return presence;
    }

    private void savePresence(UserPresence presence) {
        Duration ttl = ttlStrategy.getTtl(presence.getStatus());
        if (!ttl.isZero()) {
            presenceRedisRepository.saveWithTTL(presence, ttl);
        } else {
            presenceRedisRepository.delete(presence.getUserId());
        }
        presenceCouchbaseRepository.save(presence);
    }

    private void publishPresenceEventAsync(String userId, PresenceEventType status, String roomId, long timestamp) {
        PresenceEvent event = new PresenceEvent();
        event.setUserId(userId);
        event.setEventType(status);
        event.setRoomId(roomId);
        event.setTimestamp(timestamp);

        CompletableFuture.runAsync(() -> {
            try {
                eventPublisher.publishEvent(event);
                logProducer.sendLog(
                        "presence-service",
                        "INFO",
                        "Published presence event for user: " + userId + ", status: " + status);
            } catch (Exception e) {
                logProducer.sendLog(
                        "presence-service",
                        "ERROR",
                        "Failed to publish presence event for user: " + userId + ", error: " + e.getMessage());
            }
        });
    }
}