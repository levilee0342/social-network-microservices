package com.example.presence_service.entity;

import lombok.Data;
import org.springframework.data.couchbase.core.mapping.Document;

@Document
@Data
public class UserPresence {
    private String userId;
    private PresenceEventType status;
    private String roomId;
    private long lastActive;
    private long lastOnlineTimestamp;
}
