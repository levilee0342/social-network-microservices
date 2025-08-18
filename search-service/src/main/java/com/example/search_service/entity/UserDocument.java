package com.example.search_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.time.Instant;
import java.util.Date;

@Document
@Data
public class UserDocument {
    @Id
    String userId;
    String fullName;
    String email;
    String phone;
    String address;
    Date dob;
    String avatar;
    private String visibility; // PUBLIC, PRIVATE
    private String[] friendIds; // Danh sách ID bạn bè
    private String type = "user";
}
