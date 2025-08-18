package com.example.user_service.controller;

import com.example.user_service.dto.request.LoginRequest;
import com.example.user_service.dto.request.RegisterRequest;
import com.example.user_service.dto.request.VerifyOtpRequest;
import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.dto.response.LoginResponse;
import com.example.user_service.service.AuthService;
import com.example.user_service.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/identity")
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Validated @RequestBody RegisterRequest request) {
        authService.requestOtp(request);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("OTP sent successfully")
                        .result(null)
                        .build()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Validated @RequestBody VerifyOtpRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("User registered successfully")
                        .result(null)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.<LoginResponse>builder()
                        .code(200)
                        .message("Login successful")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Map<String, String>>builder()
                            .code(400)
                            .message("Refresh token is required")
                            .result(null)
                            .build()
            );
        }
        Map<String, String> tokens = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .code(200)
                        .message("Token refreshed successfully")
                        .result(tokens)
                        .build()
        );
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.<String>builder()
                            .code(401)
                            .message("Invalid or missing Authorization header")
                            .result(null)
                            .build()
            );
        }
        String token = authHeader.substring(7);
        if (jwtUtil.isTokenValid(token)) {
            String userId = jwtUtil.getUsernameFromToken(token);
            String storedToken = authService.getStoredToken(userId);
            if (storedToken != null && storedToken.equals(token)) {
                return ResponseEntity.ok(
                        ApiResponse.<String>builder()
                                .code(200)
                                .message("Token is valid")
                                .result(userId) // Ensure userId is a String
                                .build()
                );
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.<String>builder()
                            .code(401)
                            .message("Token does not match stored token")
                            .result(null)
                            .build()
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.<String>builder()
                        .code(401)
                        .message("Invalid token")
                        .result(null)
                        .build()
        );
    }

    @GetMapping("/maintenance")
    public ResponseEntity<ApiResponse<Map<String, String>>> getMaintenanceStatus() {
        Map<String, String> status = authService.getMaintenanceStatus();
        return ResponseEntity.ok(
                ApiResponse.<Map<String, String>>builder()
                        .code(200)
                        .message("Maintenance status retrieved successfully")
                        .result(status)
                        .build()
        );
    }

    @GetMapping("/check-user")
    public ResponseEntity<ApiResponse<Boolean>> checkExitsUser(@RequestParam("userId") String userId) {
        try {
            boolean exists = authService.CheckExitsUser(userId);

            if (exists) {
                return ResponseEntity.ok(
                        ApiResponse.<Boolean>builder()
                                .code(200)
                                .message("User tồn tại")
                                .result(true)
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<Boolean>builder()
                                .code(404)
                                .message("User không tồn tại")
                                .result(false)
                                .build()
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Boolean>builder()
                            .code(500)
                            .message("Lỗi kiểm tra user: " + e.getMessage())
                            .result(false)
                            .build()
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.<Void>builder()
                            .code(401)
                            .message("Invalid or missing Authorization header")
                            .result(null)
                            .build()
            );
        }
        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.<Void>builder()
                                .code(401)
                                .message("Invalid token")
                                .result(null)
                                .build()
                );
            }
            String userId = jwtUtil.getUsernameFromToken(token);
            authService.Logout(userId);

            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .code(200)
                            .message("Logout successful")
                            .result(null)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Void>builder()
                            .code(500)
                            .message("Logout failed: " + e.getMessage())
                            .result(null)
                            .build()
            );
        }
    }

}