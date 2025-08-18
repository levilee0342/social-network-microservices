package com.example.user_service.service.impl;

import com.example.user_service.dto.response.PublicProfileResponse;
import com.example.user_service.entity.FriendRequest;
import com.example.user_service.dto.response.FriendRequestResponse;
import com.example.user_service.dto.response.FriendResponse;
import com.example.user_service.entity.UserPresence;
import com.example.user_service.enums.PresenceEventType;
import com.example.user_service.error.AuthErrorCode;
import com.example.user_service.exception.AppException;
import com.example.user_service.kafka.producer.ProducerLog;
import com.example.user_service.repository.IRedisUserPresenceRepository;
import com.example.user_service.service.interfaces.IFriendMapperService;
import com.example.user_service.service.interfaces.IProfileService;
import org.springframework.stereotype.Service;

@Service
public class FriendMapperServiceImpl implements IFriendMapperService {

    private final IProfileService profileService;
    private final IRedisUserPresenceRepository redisUserPresenceRepository;
    private final ProducerLog logProducer;

    private static final long ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L; // 1 week in milliseconds

    public FriendMapperServiceImpl(IProfileService profileService,
                                   IRedisUserPresenceRepository redisUserPresenceRepository,
                                   ProducerLog logProducer) {
        this.profileService = profileService;
        this.redisUserPresenceRepository = redisUserPresenceRepository;
        this.logProducer = logProducer;
    }

    @Override
    public FriendRequestResponse mapToFriendRequestResponse(FriendRequest friendRequest) {
        try {
            PublicProfileResponse profile =  profileService.getPublicProfile(friendRequest.getSenderId());
            FriendRequestResponse response = FriendRequestResponse.builder()
                    .requestId(friendRequest.getRequestId())
                    .senderId(friendRequest.getSenderId())
                    .senderName(profile.getFullName())
                    .senderAvatarUrl(profile.getAvatarUrl())
                    .receiverId(friendRequest.getReceiverId())
                    .status(friendRequest.getStatus())
                    .createdAt(friendRequest.getCreatedAt())
                    .updatedAt(friendRequest.getUpdatedAt())
                    .build();
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[FriendMapper] Successfully mapped FriendRequestResponse for requestId: " +
                            friendRequest.getRequestId());
            return response;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[FriendMapper] Failed to map FriendRequestResponse for requestId: " +
                            (friendRequest != null ? friendRequest.getRequestId() : "null") + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_MAP_FRIEND);
        }
    }

    @Override
    public FriendResponse mapToFriendResponse(String friendId, long createdAt) {
        try {
            PublicProfileResponse profile = profileService.getPublicProfile(friendId);
            UserPresence presence = redisUserPresenceRepository.findByUserId(friendId);
            PresenceEventType status = presence != null ? presence.getStatus() : PresenceEventType.UNKNOWN;
            Long minutesOffline = null;
            String awayReason = null;

            if (status == PresenceEventType.OFFLINE && presence != null) {
                long timeSinceLastActive = System.currentTimeMillis() - presence.getLastActive();
                if (timeSinceLastActive > ONE_WEEK_MS) {
                    status = PresenceEventType.UNKNOWN;
                } else {
                    minutesOffline = timeSinceLastActive / (60 * 1000); // Convert to minutes
                }
            }
//            else if (status == PresenceEventType.AWAY && presence != null) {
//                awayReason = presence.getAwayReason();
//            }
            FriendResponse response = FriendResponse.builder()
                    .friendId(friendId)
                    .fullName(profile.getFullName())
                    .avatarUrl(profile.getAvatarUrl())
                    .createdAt(createdAt)
                    .status(status)
                    .minutesOffline(minutesOffline)
                    .build();
            logProducer.sendLog(
                    "user-service",
                    "DEBUG",
                    "[FriendMapper] Successfully mapped FriendResponse for friendId: " + friendId);
            return response;
        } catch (Exception e) {
            logProducer.sendLog(
                    "user-service",
                    "ERROR",
                    "[FriendMapper] Failed to map FriendResponse for friendId: " + friendId + ". Reason: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_MAP_FRIEND);
        }
    }
}