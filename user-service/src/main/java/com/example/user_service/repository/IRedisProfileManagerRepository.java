package com.example.user_service.repository;

import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.dto.response.PublicProfileResponse;

public interface IRedisProfileManagerRepository {
    void cacheProfile(String userId, ProfileResponse response);
    ProfileResponse getCachedProfile(String userId);
    void cachePublicProfile(String userId, PublicProfileResponse response);
    PublicProfileResponse getCachedPublicProfile(String userId);
    void deleteCachedProfile(String userId);
    void deleteCachedPublicProfile(String userId);
}
