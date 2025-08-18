package com.example.post_service.dto.response;

import lombok.Data;

@Data
public class PublicProfileResponse {
    private String userId;
    private String fullName;
    private String avatarUrl;
}
