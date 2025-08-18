package com.example.user_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.time.LocalDate;
import java.util.Date;

@Data
@Document
public class Profile {
    @Id
    private String profileId;
    private String userId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String address;
    private String phone;
    private Boolean gender;
    private String avatarUrl;
    private Boolean isPublic;
    private Date createdAt;
    private Date updatedAt;
}