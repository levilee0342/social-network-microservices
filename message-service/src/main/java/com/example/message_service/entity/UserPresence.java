package com.example.message_service.entity;

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonCreator;
import com.example.message_service.enums.PresenceEventType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.couchbase.core.mapping.Document;

@Document
@Data
@Builder
@NoArgsConstructor
public class UserPresence {

    private String userId;
    private PresenceEventType status;
    private String roomId;
    private long lastActive;
    private long lastOnlineTimestamp;

    @JsonCreator
    public UserPresence(
            @JsonProperty("userId") String userId,
            @JsonProperty("status") PresenceEventType status,
            @JsonProperty("roomId") String roomId,
            @JsonProperty("lastActive") long lastActive,
            @JsonProperty("lastOnlineTimestamp") long lastOnlineTimestamp
    ) {
        this.userId = userId;
        this.status = status;
        this.roomId = roomId;
        this.lastActive = lastActive;
        this.lastOnlineTimestamp = lastOnlineTimestamp;
    }
}

