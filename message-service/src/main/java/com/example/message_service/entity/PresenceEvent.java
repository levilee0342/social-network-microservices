package com.example.message_service.entity;

import com.example.message_service.enums.PresenceEventType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceEvent {
    private String userId;
    private PresenceEventType eventType;
    private String roomId;
    private long timestamp;
}
