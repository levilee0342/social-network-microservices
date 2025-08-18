package com.example.notification_service.service.interfaces;

import com.example.notification_service.dto.response.ProfileResponse;

public interface IProfileService {
    ProfileResponse getPublicProfileByUserId(String userId);
}