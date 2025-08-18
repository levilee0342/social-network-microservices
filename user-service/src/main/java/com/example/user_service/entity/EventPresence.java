package com.example.user_service.entity;

import com.example.user_service.enums.PresenceEventType;
import lombok.Data;

@Data
public class EventPresence {
    private String userId;
    private PresenceEventType eventType;
}
