package com.example.user_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class LoginResponse {
    String accessToken;
    String refreshToken;
    String userId;
    ProfileResponse profile;
}
