package com.example.notification_service.service.interfaces;

import com.example.notification_service.dto.response.PublicProfileResponse;

public interface IFriendContentGenerator {
    String generateFriendContent(String eventType, PublicProfileResponse profile);
}
