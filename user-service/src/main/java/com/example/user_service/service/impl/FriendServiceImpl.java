package com.example.user_service.service.impl;

import com.example.user_service.dto.response.FriendRequestResponse;
import com.example.user_service.dto.response.FriendResponse;
import com.example.user_service.entity.FriendRequest;
import com.example.user_service.entity.Friendship;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.error.InvalidErrorCode;
import com.example.user_service.error.NotExistedErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.kafka.producer.ProducerNotification;
import com.example.user_service.repository.ICouchbaseFriendRepository;
import com.example.user_service.service.interfaces.IFriendMapperService;
import com.example.user_service.service.interfaces.IFriendService;
import com.example.user_service.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements IFriendService {

    private final JwtUtil jwtUtil;
    private final ICouchbaseFriendRepository friendManager;
    private final IFriendMapperService friendMapper;
    private final ProducerNotification notificationService;
    private final ProducerLog logProducer;

    public FriendServiceImpl(JwtUtil jwtUtil,
                             ICouchbaseFriendRepository friendManager,
                             IFriendMapperService friendMapper,
                             ProducerNotification notificationService,
                             ProducerLog logProducer) {
        this.jwtUtil = jwtUtil;
        this.friendManager = friendManager;
        this.friendMapper = friendMapper;
        this.notificationService = notificationService;
        this.logProducer = logProducer;
    }

    @Override
    public List<FriendResponse> getFriends(@NotBlank String userId) {
        try {
            List<Friendship> friendships = friendManager.getFriendships(userId);
            List<FriendResponse> responses = friendships.stream()
                    .map(friendship -> {
                        String userId1 = friendship.getUserId1();
                        String userId2 = friendship.getUserId2();
                        String friendId;
                        if (userId1 != null && userId1.equals(userId)) {
                            friendId = userId2;
                        } else {
                            friendId = userId1;
                        }
                        return friendMapper.mapToFriendResponse(friendId, friendship.getCreatedAt());
                    })
                    .collect(Collectors.toList());
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Found " + responses.size() + " friends for userId: " + userId);
            return responses;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to fetch friends for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_FRIEND);
        }
    }

    @Override
    public List<FriendRequestResponse> getPendingFriendRequests(@NotBlank String userId) {
        try {
            List<FriendRequest> requests = friendManager.getPendingFriendRequests(userId);
            List<FriendRequestResponse> responses = requests.stream()
                    .map(friendMapper::mapToFriendRequestResponse)
                    .collect(Collectors.toList());
            logProducer.sendLog(
                    "user-service",
                    "INFO",
                    "[Friend] Found " + responses.size() + " pending friend requests for userId: " + userId);
            return responses;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to fetch pending friend requests for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_PENDING_FRIEND);
        }
    }

    @Override
    public FriendRequestResponse sendFriendRequest(@NotBlank String senderId, @NotBlank String receiverId, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(token);
        try {
            if (senderId.equals(receiverId)) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] Cannot send a friend request to yourself: " + senderId);
                throw new AppException(InvalidErrorCode.INVALID_FRIEND_REQUEST);
            }
            if(!senderId.equals(userId)) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] Logged-in user " + userId + " must be the one sending the friend request: " + senderId);
                throw new AppException(AuthErrorCode.USER_LOGIN_NOT_SEND);
            }
            if (friendManager.isFriend(senderId, receiverId)) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] Users are already friends: " + senderId + " and " + receiverId);
                throw new AppException(NotExistedErrorCode.ALREADY_FRIENDS);
            }
            if (friendManager.hasPendingRequest(senderId, receiverId)) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] User " + senderId + " has already pending request: " + receiverId);
                throw new AppException(InvalidErrorCode.FRIEND_REQUEST_PENDING);
            }
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setSenderId(senderId);
            friendRequest.setReceiverId(receiverId);
            friendRequest.setStatus("PENDING");
            friendRequest.setCreatedAt(Instant.now().getEpochSecond());
            friendManager.saveFriendRequest(friendRequest);
            notificationService.sendFriendNotification("FRIEND_REQUEST_SENT", receiverId, senderId, friendRequest.getRequestId(), token);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Friend request sent successfully from: " + senderId + " to: " + receiverId);
            return friendMapper.mapToFriendRequestResponse(friendRequest);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to send friend request from: " + senderId + " to: " + receiverId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_SEND_FRIEND_REQUEST);
        }
    }

    @Override
    public FriendRequestResponse acceptFriendRequest(@NotBlank String requestId, @NotBlank String receiverId, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        String userId = jwtUtil.getUsernameFromToken(token);
        try {
            FriendRequest friendRequest = friendManager.getFriendRequest(requestId);
            if (friendRequest == null) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[Friend] Friend request not found: " + requestId);
                throw new AppException(NotExistedErrorCode.FRIEND_REQUEST_NOT_FOUND);
            }
            if(!friendRequest.getReceiverId().equals(userId)) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[Friend] Logged-in user " + userId + " is not the one accepting the friend request: " + friendRequest.getReceiverId());
                throw new AppException(AuthErrorCode.USER_LOGIN_ACCEPT);
            }
            if (!friendRequest.getReceiverId().equals(receiverId)) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[Friend] Unauthorized access to friend request: " + requestId + " by: " + receiverId);
                throw new AppException(AuthErrorCode.UNAUTHORIZED_ACCESS_REQUEST);
            }

            if (!friendRequest.getStatus().equals("PENDING")) {
                logProducer.sendLog(
                        "user-service",
                        "ERROR",
                        "[Friend] Friend request is not pending: " + requestId);
                throw new AppException(InvalidErrorCode.INVALID_FRIEND_REQUEST_STATUS);
            }
            friendRequest.setStatus("ACCEPTED");
            friendRequest.setUpdatedAt(Instant.now().getEpochSecond());
            friendManager.updateFriendRequest(friendRequest);
            Friendship friendship = new Friendship();
            friendship.setUserId1(friendRequest.getSenderId());
            friendship.setUserId2(friendRequest.getReceiverId());
            friendship.setCreatedAt(Instant.now().getEpochSecond());
            friendManager.saveFriendship(friendship);
            notificationService.sendFriendNotification("FRIEND_REQUEST_ACCEPTED", friendRequest.getSenderId(), receiverId, friendRequest.getRequestId(), token);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Friend request accepted: " + requestId + ". Friendship created between: " + friendship.getUserId1() + " and " + friendship.getUserId2());
            return friendMapper.mapToFriendRequestResponse(friendRequest);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to accept friend request: " + requestId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_ACCEPT_FRIEND_REQUEST);
        }
    }

    @Override
    public FriendRequestResponse rejectFriendRequest(@NotBlank String requestId, @NotBlank String receiverId) {
        try {
            FriendRequest friendRequest = friendManager.getFriendRequest(requestId);
            if (friendRequest == null) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] Friend request not found: " + requestId);
                throw new AppException(NotExistedErrorCode.FRIEND_REQUEST_NOT_FOUND);
            }
            if (!friendRequest.getReceiverId().equals(receiverId)) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] Unauthorized access to friend request: " + requestId + " by: " + receiverId);
                throw new AppException(AuthErrorCode.UNAUTHORIZED_ACCESS_REQUEST);
            }
            if (!friendRequest.getStatus().equals("PENDING")) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] Friend request is not pending: " + requestId);
                throw new AppException(InvalidErrorCode.INVALID_FRIEND_REQUEST_STATUS);
            }
            friendRequest.setStatus("REJECTED");
            friendRequest.setUpdatedAt(Instant.now().getEpochSecond());
            friendManager.updateFriendRequest(friendRequest);
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Friend request rejected: " + requestId);
            return friendMapper.mapToFriendRequestResponse(friendRequest);
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to reject friend request: " + requestId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_REJECT_FRIEND_REQUEST);
        }
    }

    @Override
    public boolean removeFriend(@NotBlank String userId, @NotBlank String friendId) {
        try {
            boolean removed = friendManager.removeFriendship(userId, friendId);
            if (!removed) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] Friendship not found between: " + userId + " and " + friendId);
                throw new AppException(NotExistedErrorCode.FRIENDSHIP_NOT_FOUND);
            }
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[Friend] Friendship removed between: " + userId + " and " + friendId);
            return true;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Failed to remove friend: " + friendId + " for userId: " + userId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_REMOVE_FRIEND);
        }
    }

    @Override
    public boolean isFriend(String userId, String friendId) {
        try {
            boolean isFriend = friendManager.isFriend(userId, friendId);
            if (!isFriend) {
                logProducer.sendLog(
                        "user-service",
                        "WARN",
                        "[Friend] No friendship exists between user: " + userId + " and friend: " + friendId);
                throw new AppException(NotExistedErrorCode.FRIENDSHIP_NOT_FOUND);
            }
            logProducer.sendLog(
                    "user-service",
                    "INFO",
                    "[Friend] Friendship exists between user: " + userId + " and friend: " + friendId);
            return true;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[Friend] Error checking friendship between user: " + userId + " and friend: " + friendId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_CHECK_IS_FRIEND);
        }
    }
}
