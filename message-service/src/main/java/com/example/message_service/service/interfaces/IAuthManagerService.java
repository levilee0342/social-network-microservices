package com.example.message_service.service.interfaces;

import jakarta.servlet.http.HttpServletRequest;

public interface IAuthManagerService {
    boolean checkExitsUser(HttpServletRequest request);
}
