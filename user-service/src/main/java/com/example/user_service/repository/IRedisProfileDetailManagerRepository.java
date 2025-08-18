package com.example.user_service.repository;

import com.example.user_service.dto.response.PublicProfileDetailsResponse;

public interface IRedisProfileDetailManagerRepository {
    void cacheProfileDetail(String userId, PublicProfileDetailsResponse response);
    PublicProfileDetailsResponse getCachedProfileDetail(String userId);
    void cachePublicProfileDetail(String userId, PublicProfileDetailsResponse response);
    void deleteCachedProfileDetail(String userId);
    PublicProfileDetailsResponse getCachePublicProfileDetail(String userId);
    void deletePublicCachedProfileDetail(String userId);

}
