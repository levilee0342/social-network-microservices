package com.example.user_service.service;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.request.VerifyOtpRequest;
import com.example.user_service.dto.response.LoginResponse;
import com.example.user_service.service.interfaces.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Service
public class  AuthService {

    private final IAuthService authService;

    public AuthService(IAuthService authService) {
        this.authService = authService;
    }

    public void requestOtp(@Validated RegisterRequest request) {
        authService.requestOtp(request);
    }

    public void register(@Validated VerifyOtpRequest request) {
       authService.register(request);
    }

    public LoginResponse login(@Validated LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    public Map<String, String> refreshToken(String refreshToken) {
       return authService.refreshToken(refreshToken);
    }

    public String getStoredToken(String userId) {
        return authService.getStoredToken(userId);
    }

    public boolean CheckExitsUser(String userId) {
        return authService.CheckExitsUser(userId);
    }

    public Map<String, String> getMaintenanceStatus() {
      return authService.getMaintenanceStatus();
    }

    public void Logout(String userId) {
        authService.Logout(userId);
    }
}