package com.example.user_service.entity;

import com.example.user_service.enums.PresenceEventType;
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
