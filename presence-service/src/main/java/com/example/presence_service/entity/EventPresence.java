package com.example.presence_service.entity;

import lombok.Data;

@Data
public class EventPresence {
    private String userId;
    private PresenceEventType eventType;
}
