package com.example.message_service.service.interfaces;

import com.example.message_service.dto.request.AnonymousChatRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface IAnonymousChatService {
    void requestAnonymousChat(AnonymousChatRequest request, HttpServletRequest httprequest);
}
