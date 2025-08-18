package com.example.user_service.repository.impl;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.error.NotExistedErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseProfileRepository;
import com.example.user_service.service.interfaces.IProfileMapperService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public class CouchbaseProfileRepository implements ICouchbaseProfileRepository {

    private final Collection profileCollection;
    private final IProfileMapperService IProfileMapperService;
    private final Cluster cluster;
    private final ProducerLog logProducer;

    public CouchbaseProfileRepository(@Qualifier("profileCouchbaseCluster") Cluster cluster,
                                      @Qualifier("profileBucket") Bucket profileBucket,
                                      IProfileMapperService IProfileMapperService,
                                      ProducerLog logProducer) {
        this.cluster = cluster;
        this.profileCollection = profileBucket.scope("user_information").collection("profiles");
        this.IProfileMapperService = IProfileMapperService;
        this.logProducer = logProducer;
    }

    @Override
    public ProfileResponse createProfile(String userId, ProfileRequest request) {
        try {
            //Check profile
            String profileKey = UUID.randomUUID().toString();
            if (profileCollection.exists(profileKey).exists()) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Profile] Profile key already exists: " + profileKey);
                throw new AppException(NotExistedErrorCode.PROFILE_KEY_ALREADY_EXITS);
            }
            //Tạo profile mới
            ProfileResponse profile = new ProfileResponse();
            profile.setProfileId(UUID.randomUUID().toString());
            profile.setUserId(userId);
            profile.setFullName(request.getFullName());
            profile.setDateOfBirth(request.getDateOfBirth());
            profile.setAddress(request.getAddress());
            profile.setPhone(request.getPhone());
            profile.setAvatarUrl(request.getAvatarUrl());
            profile.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()));
            profile.setGender(Boolean.TRUE.equals(request.getGender()));
            profile.setCreatedAt(new Date());
            profile.setUpdatedAt(new Date());
            //Map dữ liệu profile
            JsonObject jsonProfile = JsonObject.create()
                    .put("profileId", profile.getProfileId())
                    .put("userId", profile.getUserId())
                    .put("fullName", profile.getFullName())
                    .put("dateOfBirth", profile.getDateOfBirth() != null ?
                            profile.getDateOfBirth().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli() : null)
                    .put("address", profile.getAddress())
                    .put("phone", profile.getPhone())
                    .put("avatarUrl", profile.getAvatarUrl())
                    .put("isPublic", profile.getIsPublic())
                    .put("gender", profile.getGender())
                    .put("createdAt", profile.getCreatedAt().getTime())
                    .put("updatedAt", profile.getUpdatedAt().getTime());
            //Lưu profile
            profileCollection.upsert(profileKey, jsonProfile);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Profile] Successfully created profile for userId: " + userId + ", profileKey: " + profileKey);
            return profile;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to create profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_CREATE_PROFILE);
        }
    }

    @Override
    public ProfileResponse getProfile(String userId) {;
        try {
            String query = "SELECT META().id, p.* FROM `profile_bucket`.`user_information`.`profiles` p WHERE p.userId = $userId";
            QueryResult result = cluster.query(query, QueryOptions.queryOptions().parameters(JsonObject.create().put("userId", userId)));
            List<JsonObject> rows = result.rowsAsObject();
            if (rows.isEmpty()) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Profile] Profile not found for userId: " + userId);
                throw new AppException(NotExistedErrorCode.PROFILE_NOT_EXITS);
            }
            JsonObject row = rows.get(0);
            ProfileResponse profile = IProfileMapperService.mapJsonToProfile(row);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Profile] Successfully retrieved profile for userId: " + userId);
            return profile;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to retrieve profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_PROFILE);
        }
    }

    @Override
    public ProfileResponse updateProfile(String userId, ProfileRequest request) {
        try {
            //Lấy profile cũ
            ProfileResponse profile = getProfile(userId);
            String profileKey = getProfileKeyByUserId(userId);
            //Cập nhật profile
            profile.setFullName(request.getFullName());
            profile.setDateOfBirth(request.getDateOfBirth());
            profile.setAddress(request.getAddress());
            profile.setPhone(request.getPhone());
            profile.setAvatarUrl(request.getAvatarUrl());
            profile.setIsPublic(Boolean.TRUE.equals(request.getIsPublic()) ? request.getIsPublic() : profile.getIsPublic());
            profile.setGender(Boolean.TRUE.equals(request.getGender()) ? request.getGender() : profile.getGender());
            profile.setUpdatedAt(new Date());
            //Map dữ liệu
            JsonObject jsonProfile = JsonObject.create()
                    .put("profileId", profile.getProfileId())
                    .put("userId", profile.getUserId())
                    .put("fullName", profile.getFullName())
                    .put("dateOfBirth", profile.getDateOfBirth() != null ?
                            profile.getDateOfBirth().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli() : null)
                    .put("address", profile.getAddress())
                    .put("phone", profile.getPhone())
                    .put("avatarUrl", profile.getAvatarUrl())
                    .put("isPublic", profile.getIsPublic())
                    .put("gender", profile.getGender())
                    .put("createdAt", profile.getCreatedAt().getTime())
                    .put("updatedAt", profile.getUpdatedAt().getTime());
            //Lưu dữ liệu
            profileCollection.upsert(profileKey, jsonProfile);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Profile] Successfully updated profile for userId: " + userId + ", profileKey: " + profileKey);
            return profile;
        } catch (Exception e) {
            logProducer.sendLog("user-service", "ERROR", "[Profile] Failed to update profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_UPDATE_PROFILE);
        }
    }

    @Override
    public boolean deleteProfile(String userId) {
        try {
            String profileKey = getProfileKeyByUserId(userId);
            if (!profileCollection.exists(profileKey).exists()) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Profile] Profile not found for userId: " + userId);
                throw new AppException(NotExistedErrorCode.PROFILE_NOT_EXITS);
            }
            profileCollection.remove(profileKey);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Profile] Successfully deleted profile for userId: " + userId + ", profileKey: " + profileKey);
            return true;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to delete profile for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_DELETE_PROFILE);
        }
    }

    @Override
    public List<ProfileResponse> getAllProfiles() {
        try {
            String query = "SELECT META().id, p.* FROM `profile_bucket`.`user_information`.`profiles` AS p";
            QueryResult result = cluster.query(query);
            List<JsonObject> rows = result.rowsAsObject();
            List<ProfileResponse> profiles = new ArrayList<>();
            for (JsonObject row : rows) {
                profiles.add(IProfileMapperService.mapJsonToProfile(row));
            }
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Profile] Successfully retrieved " + profiles.size() + " profiles");
            return profiles;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to retrieve all profiles. Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_ALL_PROFILE);
        }
    }

    private String getProfileKeyByUserId(String userId) {
        try {
            String query = "SELECT META().id FROM `profile_bucket`.`user_information`.`profiles` p WHERE p.userId = $userId";
            QueryResult result = cluster.query(query, QueryOptions.queryOptions().parameters(JsonObject.create().put("userId", userId)));
            List<JsonObject> rows = result.rowsAsObject();
            if (rows.isEmpty()) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Profile] Profile not found for userId: " + userId);
                throw new AppException(NotExistedErrorCode.PROFILE_NOT_EXITS);
            }
            String profileKey = rows.get(0).getString("id");
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Profile] Successfully retrieved profile key: " + profileKey + " for userId: " + userId);
            return profileKey;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Profile] Failed to retrieve profile key for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_PROFILE);
        }
    }
}