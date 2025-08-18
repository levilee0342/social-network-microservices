package com.example.message_service.service.impl;

import com.example.message_service.dto.request.AnonymousChatRequest;
import com.example.message_service.dto.response.ProfileResponse;
import com.example.message_service.enums.PresenceEventType;
import com.example.message_service.entity.UserPresence;
import com.example.message_service.repository.IRedisUserPresenceRedisRepository;
import com.example.message_service.service.interfaces.IChatMatchingStrategyService;
import com.example.message_service.service.interfaces.IProfileService;
import org.springframework.stereotype.Service;

@Service
public class ChatMatchingStrategyServiceImpl implements IChatMatchingStrategyService {

    private final IProfileService profileService;
    private final IRedisUserPresenceRedisRepository IRedisUserPresenceRedisRepository;

    public ChatMatchingStrategyServiceImpl(IProfileService profileService, IRedisUserPresenceRedisRepository IRedisUserPresenceRedisRepository){
        this.profileService = profileService;
        this.IRedisUserPresenceRedisRepository = IRedisUserPresenceRedisRepository;
    }

    @Override
    public boolean isMatching(AnonymousChatRequest req1, AnonymousChatRequest req2) {
        UserPresence p1 = IRedisUserPresenceRedisRepository.findByUserId(req1.getUserId());
        UserPresence p2 = IRedisUserPresenceRedisRepository.findByUserId(req2.getUserId());
        if (p1 == null || p1.getStatus() != PresenceEventType.ONLINE ||
                p2 == null || p2.getStatus() != PresenceEventType.ONLINE) {
            return false;
        }
        ProfileResponse prof1 = profileService.getProfile(req1.getUserId());
        ProfileResponse prof2 = profileService.getProfile(req2.getUserId());
        // Giới tính: true = male, false = female
        // Giả định rằng người dùng muốn ghép với người khác giới
        if (req1.isGender() == prof2.getGender() || req2.isGender() == prof1.getGender()) {
            return false;
        }
        if (req1.getBirthYear() != null && req2.getBirthYear() != null) {
            int y1 = prof1.getDateOfBirth() != null ? prof1.getDateOfBirth().getYear() : 0;
            int y2 = prof2.getDateOfBirth() != null ? prof2.getDateOfBirth().getYear() : 0;
            if (Math.abs(req1.getBirthYear() - y2) > 5 ||
                    Math.abs(req2.getBirthYear() - y1) > 5) {
                return false;
            }
        }
        if (req1.getAddress() != null && req2.getAddress() != null) {
            if (!req1.getAddress().equals(prof2.getAddress()) ||
                    !req2.getAddress().equals(prof1.getAddress())) {
                return false;
            }
        }
        return true;
    }
}
