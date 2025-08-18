package com.example.presence_service.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.couchbase.core.mapping.Document;

@Document
@Data
public class PresenceEvent {
    @JsonProperty("userId")
    private String userId;

    @JsonProperty("eventType")
    private PresenceEventType eventType;

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("timestamp")
    private long timestamp;
}
