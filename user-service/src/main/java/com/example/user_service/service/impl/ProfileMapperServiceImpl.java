package com.example.user_service.service.impl;

import com.couchbase.client.java.json.JsonObject;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.service.interfaces.IProfileMapperService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

@Service
public class ProfileMapperServiceImpl implements IProfileMapperService {

    private final ProducerLog logProducer;

    public ProfileMapperServiceImpl(ProducerLog logProducer) {
        this.logProducer = logProducer;
    }

    @Override
    public ProfileResponse mapToResponse(ProfileResponse profile) {
        try {
            ProfileResponse response = new ProfileResponse();
            response.setProfileId(profile.getProfileId());
            response.setUserId(profile.getUserId());
            response.setFullName(profile.getFullName());
            response.setDateOfBirth(profile.getDateOfBirth());
            response.setAddress(profile.getAddress());
            response.setPhone(profile.getPhone());
            response.setAvatarUrl(profile.getAvatarUrl());
            response.setIsPublic(profile.getIsPublic());
            response.setGender(profile.getGender());
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[ProfileMapper] Successfully mapped ProfileResponse for userId: " + profile.getUserId());
            return response;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[ProfileMapper] Failed to map ProfileResponse for userId: " + profile.getUserId() + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE);
        }
    }

    @Override
    public ProfileResponse mapJsonToProfile(JsonObject row) {
        logProducer.sendLog("user-service", "DEBUG", "[Profile] Mapping JSON to ProfileResponse for profileId: " + row.getString("profileId"));
        try {
            ProfileResponse profile = new ProfileResponse();
            profile.setProfileId(row.getString("profileId"));
            profile.setUserId(row.getString("userId"));
            profile.setFullName(row.getString("fullName"));
            Long dateOfBirthMillis = row.getLong("dateOfBirth");
            profile.setDateOfBirth(dateOfBirthMillis != null ?
                    Instant.ofEpochMilli(dateOfBirthMillis).atZone(ZoneId.of("UTC")).toLocalDate() : null);
            profile.setAddress(row.getString("address"));
            profile.setPhone(row.getString("phone"));
            profile.setAvatarUrl(row.getString("avatarUrl"));
            profile.setIsPublic(row.getBoolean("isPublic"));
            profile.setGender(row.getBoolean("gender"));
            profile.setCreatedAt(new Date(row.getLong("createdAt")));
            profile.setUpdatedAt(new Date(row.getLong("updatedAt")));
            logProducer.sendLog("user-service", "DEBUG", "[Profile] Successfully mapped JSON to ProfileResponse for profileId: " + row.getString("profileId"));
            return profile;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR",
                    "[Profile] Failed to map JSON to ProfileResponse for profileId: " + row.getString("profileId") + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE);
        }
    }

    @Override
    public PublicProfileResponse mapToPublicResponse(ProfileResponse profile) {
        try {
            PublicProfileResponse response = new PublicProfileResponse();
            response.setFullName(profile.getFullName());
            response.setAvatarUrl(profile.getAvatarUrl());
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[ProfileMapper] Successfully mapped PublicProfileResponse for userId: " + profile.getUserId());
            return response;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[ProfileMapper] Failed to map PublicProfileResponse for userId: " + profile.getUserId() + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_MAP_PROFILE);
        }
    }
}