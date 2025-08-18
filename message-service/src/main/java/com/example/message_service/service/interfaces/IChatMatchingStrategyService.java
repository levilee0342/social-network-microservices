package com.example.message_service.service.interfaces;

import com.example.message_service.dto.request.AnonymousChatRequest;

public interface IChatMatchingStrategyService {
    boolean isMatching(AnonymousChatRequest req1, AnonymousChatRequest req2);
}
