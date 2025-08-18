package com.example.presence_service.service.interfaces;

import com.example.presence_service.entity.PresenceEventType;

import java.time.Duration;

public interface IPresenceTtlStrategyService {
    Duration getTtl(PresenceEventType eventType);
}
