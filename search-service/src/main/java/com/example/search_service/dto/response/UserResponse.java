package com.example.search_service.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class UserResponse {
    String userId;
    String fullName;
    String email;
    String phone;
    String address;
    Date dob;
    String avatar;
}
