package com.example.user_service.controller;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @MutationMapping
    public ProfileResponse createProfile(
            Authentication authentication,
            @Argument("ProfileRequest") ProfileRequest profileRequest) {
        String userId = authentication.getName();
        System.out.println("Authenticated user ID: " + authentication.getName());
        return profileService.createProfile(userId, profileRequest);
    }

    @QueryMapping
    public List<ProfileResponse> getAllUsersWithProfile() {
        return profileService.getAllUsersWithProfile();
    }

    @QueryMapping
    public ProfileResponse getProfile(Authentication authentication) {
        String userId = authentication.getName();
        return profileService.getProfile(userId, userId);
    }

    @QueryMapping
    public Object getProfileByUserId(
            @Argument String userId,
            Authentication authentication) {

        String requestingUserId = authentication.getName();
        System.out.println("Authenticated user ID: " + requestingUserId);

        if (userId.equals(requestingUserId)) {
            return profileService.getProfile(userId, requestingUserId);
        } else {
            return profileService.getPublicProfile(userId);
        }
    }

    @QueryMapping
    public List<ProfileResponse> getProfilesByUserIds(@Argument List<String> userIds, Authentication authentication) {
        String requestingUserId = authentication.getName();
        return profileService.getProfilesByUserIds(userIds, requestingUserId);
    }


    @MutationMapping
    public ProfileResponse updateProfile(
            Authentication authentication,
            @Argument("ProfileRequest") ProfileRequest request) {

        String userId = authentication.getName();
        return profileService.updateProfile(userId, request);
    }

    @MutationMapping
    public Boolean deleteProfile(Authentication authentication) {
        String userId = authentication.getName();
        profileService.deleteProfile(userId);
        return true;
    }

    @QueryMapping
    public List<ProfileResponse> searchProfiles(@Argument String fullName, @Argument String address, @Argument String phone){
        return profileService.searchProfiles(fullName, address, phone);
    }

}
