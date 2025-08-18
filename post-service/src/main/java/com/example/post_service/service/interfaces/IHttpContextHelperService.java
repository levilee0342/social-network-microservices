package com.example.post_service.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;

public interface IHttpContextHelperService {
    String getClientIpAddress(HttpServletRequest request);
    String getUserAgent(HttpServletRequest request);
}