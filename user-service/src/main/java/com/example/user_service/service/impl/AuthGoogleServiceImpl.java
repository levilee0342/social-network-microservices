package com.example.user_service.service.impl;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.entity.User;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.ICouchbaseProfileRepository;
import com.example.user_service.repository.ICouchbaseUserRepository;
import com.example.user_service.service.interfaces.IAuthGoogleService;
import com.example.user_service.service.interfaces.IProfileMapperService;
import org.springframework.stereotype.Service;

@Service
public class AuthGoogleServiceImpl implements IAuthGoogleService {

    private final ICouchbaseUserRepository ICouchbaseUserRepository;
    private final ICouchbaseProfileRepository IProfileManager;
    private final IProfileMapperService IProfileMapperService;
    private final ProducerLog logProducer;

    public AuthGoogleServiceImpl(ICouchbaseUserRepository ICouchbaseUserRepository,
                             ICouchbaseProfileRepository IProfileManager,
                             IProfileMapperService IProfileMapperService,
                             ProducerLog logProducer) {
        this.ICouchbaseUserRepository = ICouchbaseUserRepository;
        this.IProfileManager = IProfileManager;
        this.IProfileMapperService = IProfileMapperService;
        this.logProducer = logProducer;
    }

    @Override
    public ProfileResponse createUserFromGoogle(String email, String fullName, String avatar) {
        String userKey = "user::" + email;
        try {
            // Kiểm tra xem user đã tồn tại
            if (ICouchbaseUserRepository.exists(userKey)) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Google] User already exists: " + email);
                ProfileResponse existingProfile = IProfileManager.getProfile(userKey);
                return IProfileMapperService.mapToResponse(existingProfile);
            }
            // Tạo user
            User user = new User();
            user.setUserId(email);
            user.setEmail(email);
            // Tạo profile request
            ProfileRequest profileRequest = new ProfileRequest();
            profileRequest.setFullName(fullName != null && !fullName.isEmpty() ? fullName : "Unknown");
            profileRequest.setAvatarUrl(avatar != null ? avatar : "");
            profileRequest.setIsPublic(false);
            // Lưu user và profile
            ICouchbaseUserRepository.saveUser(userKey, user);
            ProfileResponse profileResponse = IProfileManager.createProfile(email, profileRequest);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Google] User and profile created successfully: " + email);
            return profileResponse;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Google] Failed to create user: " + email + ", error: " + e.getMessage());
            throw new AppException(AuthErrorCode.USER_CREATION_FAILED);
        }
    }
}
