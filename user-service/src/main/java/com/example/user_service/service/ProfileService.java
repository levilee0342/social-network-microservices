package com.example.user_service.service;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;
import com.example.user_service.service.interfaces.IProfileService;
import com.example.user_service.service.interfaces.ISearchProfileService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class ProfileService {

    private final IProfileService profileService;
    private final ISearchProfileService searchProfileService;

    public ProfileService(IProfileService profileService,
                          ISearchProfileService searchProfileService) {
        this.profileService = profileService;
        this.searchProfileService = searchProfileService;
    }

    public ProfileResponse getProfile(String userId, String requestingUserId){
        return profileService.getProfile(userId, requestingUserId);
    }

    public List<ProfileResponse> getProfilesByUserIds(List<String> userIds, String requestingUserId){
        return profileService.getProfilesByUserIds(userIds, requestingUserId);
    }

    public PublicProfileResponse getPublicProfile(String userId){
        return profileService.getPublicProfile(userId);
    }

    public List<ProfileResponse> getAllUsersWithProfile(){
        return profileService.getAllUsersWithProfile();
    }



    public ProfileResponse createProfile(String userId, @Validated ProfileRequest request){
        return profileService.createProfile(userId, request);
    }

    public ProfileResponse updateProfile(String userId, @Validated ProfileRequest request){
        return profileService.updateProfile(userId, request);
    }

    public boolean deleteProfile(String userId){
        return profileService.deleteProfile(userId);
    }

    public List<ProfileResponse> searchProfiles(String fullName, String address, String phone){
        return searchProfileService.searchProfiles(fullName, address, phone);
    }
}
