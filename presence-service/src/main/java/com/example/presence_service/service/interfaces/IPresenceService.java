package com.example.presence_service.service.interfaces;

import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.entity.UserPresence;

public interface IPresenceService {
    void updatePresence(String userId, PresenceEventType status, String roomId);
    void setAwayStatus(String userId, String roomId);
    UserPresence getPresence(String userId);
}
