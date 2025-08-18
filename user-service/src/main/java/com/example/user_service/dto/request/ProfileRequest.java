package com.example.user_service.dto.request;

import com.couchbase.client.core.deps.com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileRequest {
    private String fullName;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private String avatarUrl;
    private Boolean gender;
    private Boolean isPublic;
}
