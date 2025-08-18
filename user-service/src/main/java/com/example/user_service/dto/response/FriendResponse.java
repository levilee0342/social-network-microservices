package com.example.user_service.dto.response;

import com.example.user_service.enums.PresenceEventType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendResponse {
    private String friendId;
    private String fullName;
    private String avatarUrl;
    private long createdAt;
    private PresenceEventType status;
    private Long minutesOffline; // Minutes since last active, only for OFFLINE
    private String awayReason; // Reason for AWAY status, null if not applicable
}
