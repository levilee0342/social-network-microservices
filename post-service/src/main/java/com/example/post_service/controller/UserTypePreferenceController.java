package com.example.post_service.controller;

import com.example.post_service.dto.request.UserTypePreferenceRequest;
import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.service.UserTypePreferenceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/user-preferences")
public class UserTypePreferenceController {

    private final UserTypePreferenceService userTypePreferenceService;

    public UserTypePreferenceController(UserTypePreferenceService userTypePreferenceService) {
        this.userTypePreferenceService = userTypePreferenceService;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createUserTypePreference(@RequestBody UserTypePreferenceRequest requestBody, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        userTypePreferenceService.saveUserPreferences(userId, requestBody);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("User type preference created")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
