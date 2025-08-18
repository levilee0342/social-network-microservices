package com.example.presence_service.service.impl;

import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.service.interfaces.IPresenceTtlStrategyService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Service
public class PresenceTtlStrategyServiceImpl implements IPresenceTtlStrategyService {
    private final Map<PresenceEventType, Duration> ttlMap;

    public PresenceTtlStrategyServiceImpl() {
        ttlMap = new EnumMap<>(PresenceEventType.class);
        ttlMap.put(PresenceEventType.ONLINE, Duration.ofMinutes(7)); // Matches FORCE_OFFLINE_THRESHOLD
        ttlMap.put(PresenceEventType.IDLE, Duration.ofMinutes(7));   // Matches FORCE_OFFLINE_THRESHOLD
        ttlMap.put(PresenceEventType.AWAY, Duration.ofDays(7));      // Long TTL for user-set AWAY
        ttlMap.put(PresenceEventType.OFFLINE, Duration.ofDays(7));   // Cache OFFLINE for 7 days
        ttlMap.put(PresenceEventType.TYPING, Duration.ofSeconds(30)); // Short TTL for TYPING
        ttlMap.put(PresenceEventType.PING, Duration.ofMinutes(1));   // Short TTL for PING
        ttlMap.put(PresenceEventType.UNKNOWN, Duration.ZERO);        // No caching for UNKNOWN
    }

    @Override
    public Duration getTtl(PresenceEventType eventType) {
        return ttlMap.getOrDefault(eventType, Duration.ofMinutes(3));
    }
}