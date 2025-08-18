package com.example.post_service.service.impl;

import com.example.post_service.service.interfaces.IHttpContextHelperService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class HttpContextHelperService implements IHttpContextHelperService {

    @Override
    public String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    @Override
    public String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}