package com.example.user_service.service.impl;

import com.example.user_service.service.interfaces.ITokenService;
import com.example.user_service.utils.JwtUtil;
import org.springframework.stereotype.Service;

@Service
public class JwtITokenServiceImpl implements ITokenService {
    private final JwtUtil jwtUtil;

    public JwtITokenServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String generateAccessToken(String userId) {
        return jwtUtil.generateAccessToken(userId);
    }

    @Override
    public String generateRefreshToken(String userId) {
        return jwtUtil.generateRefreshToken(userId);
    }

    @Override
    public boolean isTokenExpired(String token) {
        return jwtUtil.isTokenExpired(token);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
}