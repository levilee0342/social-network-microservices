package com.example.post_service.service;

import com.example.post_service.dto.request.UserTypePreferenceRequest;
import com.example.post_service.service.interfaces.IUserTypePreferenceService;
import org.springframework.stereotype.Service;

@Service
public class UserTypePreferenceService {

    private final IUserTypePreferenceService userTypePreferenceService;

    public UserTypePreferenceService(IUserTypePreferenceService userTypePreferenceService) {
        this.userTypePreferenceService = userTypePreferenceService;
    }

    public void saveUserPreferences(String userId, UserTypePreferenceRequest request){
        userTypePreferenceService.saveUserPreferences(userId, request);
    }
}
