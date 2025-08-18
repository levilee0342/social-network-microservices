package com.example.user_service.service.interfaces;

import com.example.user_service.dto.response.FriendRequestResponse;
import com.example.user_service.dto.response.FriendResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public interface IFriendService {
    List<FriendResponse> getFriends(@NotBlank String userId);
    List<FriendRequestResponse> getPendingFriendRequests(@NotBlank String userId);
    FriendRequestResponse sendFriendRequest(@NotBlank String senderId, @NotBlank String receiverId, HttpServletRequest httpRequest);
    FriendRequestResponse acceptFriendRequest(@NotBlank String requestId, @NotBlank String receiverId, HttpServletRequest httpRequest);
    FriendRequestResponse rejectFriendRequest(@NotBlank String requestId, @NotBlank String receiverId);
    boolean removeFriend(@NotBlank String userId, @NotBlank String friendId);
    boolean isFriend(String userId, String friendId);

}
