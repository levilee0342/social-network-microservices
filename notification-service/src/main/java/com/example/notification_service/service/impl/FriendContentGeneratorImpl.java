package com.example.notification_service.service.impl;

import com.example.notification_service.dto.response.PublicProfileResponse;
import com.example.notification_service.service.interfaces.IFriendContentGenerator;
import org.springframework.stereotype.Service;

@Service
public class FriendContentGeneratorImpl implements IFriendContentGenerator {
    @Override
    public String generateFriendContent(String eventType, PublicProfileResponse profile) {
        return switch (eventType) {
            case "FRIEND_REQUEST_SENT" -> profile != null ?
                    String.format("%s đã gửi lời mời kết bạn.", profile.getFullName()) :
                    "Someone sent you a friend request.";
            case "FRIEND_REQUEST_ACCEPTED" -> profile != null ?
                    String.format("%s đã chấp nhận lời mời kết bạn.", profile.getFullName()) :
                    "Someone accepted your friend request.";
            default -> "New friend event occurred.";
        };
    }
}