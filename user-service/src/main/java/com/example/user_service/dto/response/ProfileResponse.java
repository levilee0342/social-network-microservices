package com.example.user_service.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Data
public class ProfileResponse {
    private String profileId;
    private String userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private String avatarUrl;
    private Boolean gender;
    private Boolean isPublic;
    private Date createdAt;
    private Date updatedAt;
}
