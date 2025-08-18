package com.example.user_service.service.impl;

import com.example.user_service.dto.request.profile_details.*;
import com.example.user_service.dto.response.PublicProfileDetailsResponse;
import com.example.user_service.dto.response.profile_details.*;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseProfileDetailRepository;
import com.example.user_service.repository.IRedisProfileDetailManagerRepository;
import com.example.user_service.service.interfaces.IProfileDetailsMapperService;
import com.example.user_service.service.interfaces.IProfileDetailsService;
import org.hibernate.cache.CacheException;
import org.springframework.stereotype.Service;

@Service
public class ProfileDetailsServiceImpl implements IProfileDetailsService {
    private final ICouchbaseProfileDetailRepository profileRepository;
    private final IRedisProfileDetailManagerRepository cacheManager;
    private final IProfileDetailsMapperService profileMapper;
    private final ProducerLog logProducer;

    public ProfileDetailsServiceImpl(ICouchbaseProfileDetailRepository profileRepository,
                                     IRedisProfileDetailManagerRepository cacheManager,
                                     IProfileDetailsMapperService profileMapper,
                                     ProducerLog logProducer) {
        this.profileRepository = profileRepository;
        this.cacheManager = cacheManager;
        this.profileMapper = profileMapper;
        this.logProducer = logProducer;
    }

    @Override
    public PublicProfileDetailsResponse findByUserId(String userId) {
        try {
            PublicProfileDetailsResponse cachedProfile = cacheManager.getCachePublicProfileDetail(userId);
            if (cachedProfile != null) {
                logProducer.sendLog("user-service", "INFO",
                        "[ProfileService] Lấy hồ sơ công khai từ cache cho userId: " + userId);
                return cachedProfile;
            }
        } catch (CacheException e) {
            logProducer.sendLog("user-service", "WARN",
                    "[ProfileService] Lỗi khi lấy hồ sơ công khai từ cache cho userId: " + userId + ". Tiếp tục lấy từ Couchbase.");
        }

        // Nếu không có trong cache, lấy từ Couchbase
        PublicProfileDetailsResponse profile = profileRepository.findByUserId(userId);
        if (profile == null) {
            logProducer.sendLog("user-service", "INFO",
                    "[ProfileService] Không tìm thấy hồ sơ cho userId: " + userId);
            return null;
        }

        // Ánh xạ và lưu vào cache
        PublicProfileDetailsResponse response = profileMapper.mapToResponse(profile);
        try {
            cacheManager.cachePublicProfileDetail(userId, response);
        } catch (CacheException e) {
            logProducer.sendLog("user-service", "WARN",
                    "[ProfileService] Lưu hồ sơ công khai vào cache thất bại cho userId: " + userId);
        }
        return response;
    }

    @Override
    public PublicProfileDetailsResponse getOwnProfile(String userId) {
        try {
            PublicProfileDetailsResponse cachedProfile = cacheManager.getCachedProfileDetail(userId);
            if (cachedProfile != null) {
                logProducer.sendLog("user-service", "INFO",
                        "[ProfileService] Lấy hồ sơ chi tiết từ cache cho userId: " + userId);
                return cachedProfile;
            }
        } catch (CacheException e) {
            logProducer.sendLog("user-service", "WARN",
                    "[ProfileService] Lỗi khi lấy hồ sơ chi tiết từ cache cho userId: " + userId + ". Tiếp tục lấy từ Couchbase.");
        }

        // Nếu không có trong cache, lấy từ Couchbase
        PublicProfileDetailsResponse profile = profileRepository.findByUserId(userId);
        if (profile == null) {
            logProducer.sendLog("user-service", "INFO",
                    "[ProfileService] Không tìm thấy hồ sơ cho userId: " + userId);
            return null;
        }

        // Ánh xạ và lưu vào cache
        PublicProfileDetailsResponse response = profileMapper.mapToResponseForOwner(profile);
        try {
            cacheManager.cacheProfileDetail(userId, response);
        } catch (CacheException e) {
            logProducer.sendLog("user-service", "WARN",
                    "[ProfileService] Lưu hồ sơ chi tiết vào cache thất bại cho userId: " + userId);
        }
        return response;
    }

    @Override
    public PublicProfileDetailsResponse createDefaultProfileDetails(String userId) {
        PublicProfileDetailsResponse created = profileRepository.createDefaultProfileDetails(userId);
        PublicProfileDetailsResponse response = profileMapper.mapToResponseForOwner(created);

        // Lưu vào cache
        try {
            cacheManager.cacheProfileDetail(userId, response);
            cacheManager.cachePublicProfileDetail(userId, profileMapper.mapToResponse(created));
            logProducer.sendLog("user-service", "INFO",
                    "[ProfileService] Lưu hồ sơ mới vào cache thành công cho userId: " + userId);
        } catch (CacheException e) {
            logProducer.sendLog("user-service", "WARN",
                    "[ProfileService] Lưu hồ sơ mới vào cache thất bại cho userId: " + userId);
        }
        return response;
    }

    @Override
    public EducationResponse addEducation(String userId, EducationRequest request) {
        EducationResponse response = profileRepository.addEducation(userId, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public EducationResponse updateEducationByIndex(String userId, int index, EducationRequest request) {
        EducationResponse response = profileRepository.updateEducationByIndex(userId, index, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public boolean deleteEducationByIndex(String userId, int index) {
        boolean deleted = profileRepository.deleteEducationByIndex(userId, index);

        refreshCache(userId);

        return deleted;
    }

    @Override
    public WorkResponse addWork(String userId, WorkRequest request) {
        WorkResponse response = profileRepository.addWork(userId, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public WorkResponse updateWorkByIndex(String userId, int index, WorkRequest request){
        WorkResponse response = profileRepository.updateWorkByIndex(userId, index, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public boolean deleteWorkByIndex(String userId, int index) {
        boolean deleted = profileRepository.deleteWorkByIndex(userId, index);

        refreshCache(userId);

        return deleted;
    }

    @Override
    public FamilyMemberResponse addFamilyMember(String userId, FamilyMemberRequest request){
        FamilyMemberResponse response = profileRepository.addFamilyMember(userId, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public FamilyMemberResponse updateFamilyMemberByIndex(String userId, int index, FamilyMemberRequest request){
        FamilyMemberResponse response = profileRepository.updateFamilyMemberByIndex(userId, index, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public boolean deleteFamilyMemberByIndex(String userId, int index){
        boolean deleted = profileRepository.deleteFamilyMemberByIndex(userId, index);

        refreshCache(userId);

        return deleted;
    }

    @Override
    public LocationResponse updateLocation(String userId, LocationRequest request) {
        LocationResponse response = profileRepository.updateLocation(userId, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public ContactResponse updateContact(String userId, ContactRequest request) {
        ContactResponse response = profileRepository.updateContact(userId, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public RelationshipResponse updateRelationship(String userId, RelationshipRequest request) {
        RelationshipResponse response = profileRepository.updateRelationship(userId, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public DetailResponse updateDetail(String userId, DetailRequest request) {
        DetailResponse response = profileRepository.updateDetail(userId, request);

        refreshCache(userId);

        return response;
    }

    @Override
    public boolean deleteLocationField(String userId, String fieldToDelete) {
        boolean deleted = profileRepository.deleteLocationField(userId, fieldToDelete);

        refreshCache(userId);

        return deleted;
    }

    @Override
    public boolean deleteContactField(String userId, String fieldName, String socialKey) {
        boolean deleted = profileRepository.deleteContactField(userId, fieldName, socialKey);

        refreshCache(userId);

        return deleted;
    }

    @Override
    public boolean deleteRelationship(String userId) {
        boolean deleted = profileRepository.deleteRelationship(userId);

        refreshCache(userId);

        return deleted;
    }

    @Override
    public boolean deleteDetailField(String userId, String fieldToDelete) {
        boolean deleted = profileRepository.deleteDetailField(userId, fieldToDelete);

        refreshCache(userId);

        return deleted;
    }

    private void refreshCache(String userId) {
        try {
            PublicProfileDetailsResponse profile = profileRepository.findByUserId(userId);
            if (profile != null) {
                cacheManager.cacheProfileDetail(userId, profileMapper.mapToResponseForOwner(profile));
                cacheManager.cachePublicProfileDetail(userId, profileMapper.mapToResponse(profile));
                logProducer.sendLog("user-service", "INFO",
                        "[ProfileService] Làm mới cache thành công cho userId: " + userId);
            } else {
                cacheManager.deleteCachedProfileDetail(userId);
                cacheManager.deletePublicCachedProfileDetail(userId);
                logProducer.sendLog("user-service", "INFO",
                        "[ProfileService] Xóa cache vì không tìm thấy hồ sơ cho userId: " + userId);
            }
        } catch (CacheException e) {
            logProducer.sendLog("user-service", "WARN",
                    "[ProfileService] Làm mới cache thất bại cho userId: " + userId);
        }
    }

}
