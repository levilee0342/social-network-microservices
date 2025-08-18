package com.example.notification_service.service.impl;

import com.example.notification_service.dto.request.UserPreferenceRequest;
import com.example.notification_service.dto.response.UserPreferenceResponse;
import com.example.notification_service.entity.UserPreference;
import com.example.notification_service.error.AuthErrorCode;
import com.example.notification_service.kafka.producer.ProducerLog;
import com.example.notification_service.repository.UserPreferenceRepository;
import com.example.notification_service.service.interfaces.IUserPreferenceService;
import com.example.notification_service.exception.AppException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserPreferenceServiceImpl implements IUserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final ProducerLog producerLog;

    public UserPreferenceServiceImpl(UserPreferenceRepository userPreferenceRepository, ProducerLog producerLog) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.producerLog = producerLog;
    }

    @Override
    @Transactional
    public UserPreferenceResponse updatePreferences(String userId, UserPreferenceRequest request) {
        try {
            UserPreference preference = getOrCreateUserPreference(userId);
            preference.setEmailNotifications(request.isEmailNotifications());
            preference.setPushNotifications(request.isPushNotifications());
            preference.setInAppNotifications(request.isInAppNotifications());

            preference = userPreferenceRepository.save(preference);
            producerLog.sendLog(
                    "notification-service",
                    "INFO",
                    "[Update Preferences] Updated preferences for userId: " + userId);

            return mapToUserPreferenceResponse(preference);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Update Preferences] Failed to update preferences for userId: " + userId + ". Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_UPDATE_NOTIFICATION);
        }
    }

    @Override
    public UserPreferenceResponse getPreferences(String userId) {
        try {
            UserPreference preference = getOrCreateUserPreference(userId);
            return mapToUserPreferenceResponse(preference);
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Get Preferences] Failed to get preferences for userId: " + userId + ". Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_GET_NOTIFICATION);
        }
    }

    private UserPreference getOrCreateUserPreference(String userId) {
        try {
            return userPreferenceRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        producerLog.sendLog(
                                "notification-service",
                                "INFO",
                                "[Create Preference] Creating new preference for userId: " + userId);
                        UserPreference newPref = new UserPreference();
                        newPref.setUserId(userId);
                        try {
                            return userPreferenceRepository.save(newPref);
                        } catch (DataIntegrityViolationException e) {
                            producerLog.sendLog(
                                    "notification-service",
                                    "WARN",
                                    "[Create Preference] Race condition detected for userId: " + userId + ". Retrying...");
                            return userPreferenceRepository.findByUserId(userId)
                                    .orElseThrow(() -> {
                                        producerLog.sendLog("notification-service", "ERROR", "[Create Preference] Failed to create or find preference for userId: " + userId + ". Reason: " + e);
                                        return new AppException(AuthErrorCode.FAILED_CREATE_OR_FIND_PREFERENCE);
                                    });
                        }
                    });
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[GetOrCreate Preference] Failed to get or create preference for userId: " + userId + ". Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_GET_OR_CREATE_PREFERENCE);
        }
    }

    private UserPreferenceResponse mapToUserPreferenceResponse(UserPreference preference) {
        try {
            UserPreferenceResponse response = new UserPreferenceResponse();
            response.setId(preference.getId());
            response.setUserId(preference.getUserId().toString());
            response.setEmailNotifications(preference.isEmailNotifications());
            response.setPushNotifications(preference.isPushNotifications());
            response.setInAppNotifications(preference.isInAppNotifications());
            response.setUpdatedAt(preference.getUpdatedAt());
            return response;
        } catch (Exception e) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Map Preference] Failed to map preference for user. Reason: " + e);
            throw new AppException(AuthErrorCode.FAILED_MAP_PREFERENCE);
        }
    }
}
