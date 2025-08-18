package com.example.message_service.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class WebSocketSessionRegistry {
    private final ConcurrentMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    public void register(String sessionId, String userId) {
        sessionUserMap.put(sessionId, userId);
    }

    public String getUser(String sessionId) {
        return sessionUserMap.get(sessionId);
    }

    public void unregister(String sessionId) {
        sessionUserMap.remove(sessionId);
    }
}
