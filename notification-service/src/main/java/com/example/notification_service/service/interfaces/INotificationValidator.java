package com.example.notification_service.service.interfaces;

public interface INotificationValidator {
    String validateUserId(String userId);
    String validateToken(String token);
}