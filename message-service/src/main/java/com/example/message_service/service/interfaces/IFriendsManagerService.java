package com.example.message_service.service.interfaces;

public interface IFriendsManagerService {
    boolean isFriend(String userId, String postUserId, String token);
}
