package com.example.message_service.service.interfaces;

import com.example.message_service.dto.response.ProfileResponse;

public interface IProfileService {
    ProfileResponse getProfile(String token);
}