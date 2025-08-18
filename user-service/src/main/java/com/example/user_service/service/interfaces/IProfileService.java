package com.example.user_service.service.interfaces;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;
import org.springframework.validation.annotation.Validated;

import java.util.List;

public interface IProfileService {
    ProfileResponse getProfile(String userId, String requestingUserId);
    List<ProfileResponse> getProfilesByUserIds(List<String> userIds, String requestingUserId);
    PublicProfileResponse getPublicProfile(String userId);
    List<ProfileResponse> getAllUsersWithProfile();
    ProfileResponse createProfile(String userId, @Validated ProfileRequest request);
    ProfileResponse updateProfile(String userId, @Validated ProfileRequest request);
    boolean deleteProfile(String userId);
}
