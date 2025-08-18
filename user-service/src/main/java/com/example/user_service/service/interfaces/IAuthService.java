package com.example.user_service.service.interfaces;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.request.VerifyOtpRequest;
import com.example.user_service.dto.response.LoginResponse;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

public interface IAuthService {
    void requestOtp(@Validated RegisterRequest request);
    void register(@Validated VerifyOtpRequest request);
    LoginResponse login(@Validated LoginRequest loginRequest);
    Map<String, String> refreshToken(String refreshToken);
    String getStoredToken(String userId);
    boolean CheckExitsUser(String userId);
    Map<String, String> getMaintenanceStatus();
    void Logout(String userId);
}
