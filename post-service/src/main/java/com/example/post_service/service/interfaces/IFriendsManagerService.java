package com.example.post_service.service.interfaces;

import java.util.List;

public interface IFriendsManagerService {
    boolean isFriend(String userId, String postUserId, String token);
    List<String> getFriendIds(String userId, String token);
}
