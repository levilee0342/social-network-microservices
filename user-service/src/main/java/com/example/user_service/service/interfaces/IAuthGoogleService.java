package com.example.user_service.service.interfaces;

import com.example.user_service.dto.response.ProfileResponse;

public interface IAuthGoogleService {
    ProfileResponse createUserFromGoogle(String email, String fullName, String avatar);
}
