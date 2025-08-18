package com.example.user_service.service;

import com.example.user_service.dto.response.ProfileResponse;
import com.example.user_service.service.interfaces.IAuthGoogleService;
import org.springframework.stereotype.Service;

@Service
public class AuthGoogleService {

    private final IAuthGoogleService authGoogleService;

    public AuthGoogleService(IAuthGoogleService authGoogleService) {
        this.authGoogleService = authGoogleService;
    }

    public ProfileResponse createUserFromGoogle(String email, String fullName, String avatar){
        return authGoogleService.createUserFromGoogle(email, fullName, avatar);
    }
}