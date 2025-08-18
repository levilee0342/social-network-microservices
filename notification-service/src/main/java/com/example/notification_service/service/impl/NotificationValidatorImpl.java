package com.example.notification_service.service.impl;

import com.example.notification_service.error.InvalidErrorCode;
import com.example.notification_service.exception.AppException;
import com.example.notification_service.kafka.producer.ProducerLog;
import com.example.notification_service.service.interfaces.INotificationValidator;
import org.springframework.stereotype.Service;

@Service
public class NotificationValidatorImpl implements INotificationValidator {
    private final ProducerLog producerLog;

    public NotificationValidatorImpl(ProducerLog producerLog) {
        this.producerLog = producerLog;
    }

    @Override
    public String validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Validation] Invalid userId format: " + userId);
            throw new AppException(InvalidErrorCode.INVALID_USERID);
        }
        return userId;
    }

    @Override
    public String validateToken(String token) {
        if (token == null || token.isBlank()) {
            producerLog.sendLog(
                    "notification-service",
                    "ERROR",
                    "[Validation] Invalid Token format: " + token);
            throw new AppException(InvalidErrorCode.INVALID_TOKEN);
        }
        return token;
    }
}