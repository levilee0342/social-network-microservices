package com.example.post_service.entity;

import lombok.Data;

@Data
public class Event {
    private String userId;
    private String targetUserId;
    private String postId;
    private long timestamp;
    private String type;
}
