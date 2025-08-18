package com.example.notification_service.dto.response;

import lombok.Data;

@Data
public class ProfileResponse {
    private String userId;
    private String fullName;
    private String email;
    private String avatarUrl;
}
