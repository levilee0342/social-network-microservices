package com.example.message_service.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProfileResponse {
    private String profileId;
    private String userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private String avatarUrl;
    private Boolean isPublic;
    private Boolean gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
