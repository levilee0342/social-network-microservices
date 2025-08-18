package com.example.user_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FriendRequestResponse {
private String requestId;
private String senderId;
private String senderName;
private String senderAvatarUrl;
private String receiverId;
private String status;
private long createdAt;
private long updatedAt;
}
