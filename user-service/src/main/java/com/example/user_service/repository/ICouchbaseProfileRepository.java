package com.example.user_service.repository;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.ProfileResponse;

import java.util.List;

public interface ICouchbaseProfileRepository {
    ProfileResponse createProfile(String userId, ProfileRequest request);
    ProfileResponse getProfile(String userId);
    ProfileResponse updateProfile(String userId, ProfileRequest request);
    boolean deleteProfile(String userId);
    List<ProfileResponse> getAllProfiles();
}
