package com.example.notification_service.service.impl;

import com.example.notification_service.dto.response.NotificationResponse;
import com.example.notification_service.entity.Notification;
import com.example.notification_service.error.AuthErrorCode;
import com.example.notification_service.exception.AppException;
import com.example.notification_service.service.interfaces.INotificationMapper;
import org.springframework.stereotype.Service;

@Service
public class NotificationMapperImpl implements INotificationMapper {

    @Override
    public NotificationResponse mapToNotificationResponse(Notification notification) {
        try {
            NotificationResponse response = new NotificationResponse();
            response.setId(notification.getId());
            response.setUserId(notification.getUserId());
            response.setType(notification.getType());
            response.setContent(notification.getContent());
            response.setRead(notification.isRead());
            response.setCreatedAt(notification.getCreatedAt());
            response.setRelatedEntityId(notification.getRelatedEntityId());
            response.setRelatedEntityType(notification.getRelatedEntityType());
            response.setActorIds(notification.getActorIds());
            return response;
        } catch (Exception e) {
            throw new AppException(AuthErrorCode.FAILED_MAP_NOTIFICATION);
        }
    }
}
