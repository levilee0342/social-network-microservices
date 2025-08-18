package com.example.message_service.service.interfaces;

import com.example.message_service.dto.request.AnonymousChatRequest;

public interface IChatRequestValidatorService {
    void validate(AnonymousChatRequest request);
}
