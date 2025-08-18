package com.example.user_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

@Data
@Document
public class FriendRequest {
    @Id
    private String requestId;
    private String senderId;
    private String receiverId;
    private String status; // PENDING, ACCEPTED, REJECTED
    private long createdAt;
    private long updatedAt;
}
