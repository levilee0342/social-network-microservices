package com.example.post_service.utils;

import jakarta.servlet.http.HttpServletRequest;

public class JwtUtils {
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
