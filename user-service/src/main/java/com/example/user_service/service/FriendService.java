package com.example.user_service.service;

import com.example.user_service.dto.response.FriendRequestResponse;
import com.example.user_service.dto.response.FriendResponse;
import com.example.user_service.service.interfaces.IFriendService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@Service
@Validated
public class FriendService {

    private final IFriendService friendService;

    public FriendService(IFriendService friendService) {
        this.friendService = friendService;
    }

    public FriendRequestResponse sendFriendRequest(@NotBlank String senderId, @NotBlank String receiverId, HttpServletRequest httpRequest) {
        return friendService.sendFriendRequest(senderId, receiverId, httpRequest);
    }

    public FriendRequestResponse acceptFriendRequest(@NotBlank String requestId, @NotBlank String receiverId, HttpServletRequest httpRequest) {
        return friendService.acceptFriendRequest(requestId, receiverId, httpRequest);
    }

    public FriendRequestResponse rejectFriendRequest(@NotBlank String requestId, @NotBlank String receiverId) {
        return friendService.rejectFriendRequest(requestId, receiverId);
    }

    public List<FriendResponse> getFriends(@NotBlank String userId) {
        return friendService.getFriends(userId);
    }

    public List<FriendRequestResponse> getPendingFriendRequests(@NotBlank String userId) {
        return friendService.getPendingFriendRequests(userId);
    }

    public boolean removeFriend(@NotBlank String userId, @NotBlank String friendId) {
        return friendService.removeFriend(userId, friendId);
    }

    public boolean isFriend(String userId, String friendId) {
        return friendService.isFriend(userId, friendId);
    }
}