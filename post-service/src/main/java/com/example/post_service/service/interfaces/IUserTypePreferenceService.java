package com.example.post_service.service.interfaces;

import com.example.post_service.dto.request.UserTypePreferenceRequest;

public interface IUserTypePreferenceService {
    void saveUserPreferences(String userId, UserTypePreferenceRequest request);
}
