package com.example.user_service.service.interfaces;

import com.example.user_service.entity.FriendRequest;
import com.example.user_service.dto.response.FriendRequestResponse;
import com.example.user_service.dto.response.FriendResponse;

public interface IFriendMapperService {
    FriendRequestResponse mapToFriendRequestResponse(FriendRequest friendRequest);
    FriendResponse mapToFriendResponse(String friendId, long createdAt);
}
