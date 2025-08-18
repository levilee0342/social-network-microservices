package com.example.user_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginGoogleResponse {
    private String accessToken;
    private UserResponse userData;
    private boolean isNewUser;
}
