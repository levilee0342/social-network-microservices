package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.ProfileResponse;

public interface IProfileService {
    ProfileResponse getProfile(String token);
    ProfileResponse getPublicProfileByUserId(String userId);
}