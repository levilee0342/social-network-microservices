package com.example.message_service.service.impl;

import com.example.message_service.dto.request.AnonymousChatRequest;
import com.example.message_service.error.InvalidErrorCode;
import com.example.message_service.exception.AppException;
import com.example.message_service.service.interfaces.IChatRequestValidatorService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Service
public class ChatRequestValidatorServiceImpl implements IChatRequestValidatorService {

    private static final List<String> VIETNAM_PROVINCES = List.of("Hà Nội", "TP Hồ Chí Minh", "Đà Nẵng", "Hải Phòng", "Cần Thơ", "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Yên Bái");

    @Override
    public void validate(AnonymousChatRequest request) {
        if (request.getBirthYear() != null) {
            int currentYear = Instant.now().atZone(ZoneId.of("UTC")).getYear();
            if (request.getBirthYear() < 1900 || request.getBirthYear() > currentYear) {
                throw new AppException(InvalidErrorCode.INVALID_BIRTH_YEAR);
            }
        }

        if (request.getAddress() != null && !VIETNAM_PROVINCES.contains(request.getAddress())) {
            throw new AppException(InvalidErrorCode.INVALID_ADDRESS);
        }
    }
}
