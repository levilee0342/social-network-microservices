package com.example.user_service.service.interfaces;

public interface ITokenService {
    String generateAccessToken(String userId);
    String generateRefreshToken(String userId);
    boolean isTokenExpired(String token);
    String getUsernameFromToken(String token);
}
