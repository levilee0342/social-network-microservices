package com.example.user_service.service.impl;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseProfileRepository;
import com.example.user_service.repository.IRedisProfileManagerRepository;
import com.example.user_service.service.interfaces.IProfileMapperService;
import com.example.user_service.service.interfaces.IProfileService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
public class ProfileServiceImpl implements IProfileService {

    private final ICouchbaseProfileRepository IProfileManager;
    private final IRedisProfileManagerRepository cacheManager;
    private final IProfileMapperService IProfileMapperService;
    private final ProducerLog logProducer;

    public ProfileServiceImpl(ICouchbaseProfileRepository IProfileManager,
                              IRedisProfileManagerRepository cacheManager,
                              IProfileMapperService IProfileMapperService,
                              ProducerLog logProducer) {
        this.IProfileManager = IProfileManager;
        this.cacheManager = cacheManager;
        this.IProfileMapperService = IProfileMapperService;
        this.logProducer = logProducer;
    }

    @Override
    public ProfileResponse getProfile(String userId, String requestingUserId) {
        try {
            ProfileResponse cachedProfile = cacheManager.getCachedProfile(userId);
            if (cachedProfile != null) {
                if (!cachedProfile.getUserId().equals(requestingUserId) && !cachedProfile.getIsPublic()) {
                    logProducer.sendLog(
                            "user-service",
                            "WARN",
                            "[Profile] Access denied: profile is private for userId: " + userId);
                    throw new AppException(AuthErrorCode.PROFILE_PRIVATE);
                }
                return cachedProfile;
            }
            ProfileResponse profile = IProfileManager.getProfile(userId);
            if (!profile.getUserId().equals(requestingUserId) && !profile.getIsPublic()) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Profile] Access denied: profile is private for userId: " + userId);
                throw new AppException(AuthErrorCode.PROFILE_PRIVATE);
            }
            ProfileResponse response = IProfileMapperService.mapToResponse(profile);
            cacheManager.cacheProfile(userId, response);
            return response;
        } catch (AppException ae) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Access violation when fetching profile for userId: " + userId + ". Reason: " + ae.getMessage());
            throw ae;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to fetch profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ProfileResponse> getProfilesByUserIds(List<String> userIds, String requestingUserId) {
        return userIds.stream()
                .map(userId -> {
                    try {
                        return getProfile(userId, requestingUserId);
                    } catch (AppException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }


    @Override
    public PublicProfileResponse getPublicProfile(String userId) {
        try {
            PublicProfileResponse cached = cacheManager.getCachedPublicProfile(userId);
            if (cached != null) {
                return cached;
            }
            ProfileResponse profile = IProfileManager.getProfile(userId);
            PublicProfileResponse response = IProfileMapperService.mapToPublicResponse(profile);
            cacheManager.cachePublicProfile(userId, response);
            return response;
        } catch (AppException ae) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Access violation on public profile for userId: " + userId + ". Reason: " + ae.getMessage());
            throw ae;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to fetch public profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ProfileResponse> getAllUsersWithProfile() {
        try {
            List<ProfileResponse> profiles = IProfileManager.getAllProfiles();
            List<ProfileResponse> responses = profiles.stream()
                    .map(IProfileMapperService::mapToResponse)
                    .toList();
            for (ProfileResponse response : responses) {
                cacheManager.cacheProfile(response.getUserId(), response);
                if (response.getIsPublic()) {
                    PublicProfileResponse publicResponse = IProfileMapperService.mapToPublicResponse(IProfileManager.getProfile(response.getUserId()));
                    cacheManager.cachePublicProfile(response.getUserId(), publicResponse);
                }
            }
            return responses;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to get all profiles. Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public ProfileResponse createProfile(String userId, @Validated ProfileRequest request) {
        try {
            ProfileResponse profile = IProfileManager.createProfile(userId, request);
            ProfileResponse response = IProfileMapperService.mapToResponse(profile);
            cacheManager.cacheProfile(userId, response);
            if (profile.getIsPublic()) {
                PublicProfileResponse publicResponse = IProfileMapperService.mapToPublicResponse(profile);
                cacheManager.cachePublicProfile(userId, publicResponse);
            }
            return response;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to create profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public ProfileResponse updateProfile(String userId, @Validated ProfileRequest request) {
        try {
            ProfileResponse profile = IProfileManager.updateProfile(userId, request);
            ProfileResponse response = IProfileMapperService.mapToResponse(profile);
            cacheManager.cacheProfile(userId, response);
            if (profile.getIsPublic()) {
                PublicProfileResponse publicResponse = IProfileMapperService.mapToPublicResponse(profile);
                cacheManager.cachePublicProfile(userId, publicResponse);
            } else {
                cacheManager.deleteCachedPublicProfile(userId);
            }
            return response;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to update profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }

    public boolean deleteProfile(String userId) {
        try {
            boolean deleted = IProfileManager.deleteProfile(userId);
            cacheManager.deleteCachedProfile(userId);
            cacheManager.deleteCachedPublicProfile(userId);
            return deleted;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to delete profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw e;
        }
    }
}
