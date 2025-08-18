package com.example.notification_service.service.interfaces;

import com.example.notification_service.dto.request.UserPreferenceRequest;
import com.example.notification_service.dto.response.UserPreferenceResponse;

public interface IUserPreferenceService {
    UserPreferenceResponse updatePreferences(String userId, UserPreferenceRequest request);
    UserPreferenceResponse getPreferences(String userId);
}