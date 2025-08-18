package com.example.user_service.repository;

import com.example.user_service.entity.FriendRequest;
import com.example.user_service.entity.Friendship;

import java.util.List;

public interface ICouchbaseFriendRepository {
    void saveFriendRequest(FriendRequest friendRequest);
    FriendRequest getFriendRequest(String requestId);
    void updateFriendRequest(FriendRequest friendRequest);
    boolean hasPendingRequest(String senderId, String receiverId);
    List<FriendRequest> getPendingFriendRequests(String userId);
    void saveFriendship(Friendship friendship);
    List<Friendship> getFriendships(String userId);
    boolean isFriend(String userId1, String userId2);
    boolean removeFriendship(String userId1, String userId2);
}
