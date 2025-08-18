package com.example.notification_service.controller;

import com.example.notification_service.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketNotificationController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Thông báo riêng cho user về bài viết
    public void sendPostNotification(String userId, Notification notification) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    // Thông báo riêng cho user về bạn bè
    public void sendFriendNotification(String userId, Notification notification) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/friend-notifications", notification);
    }
}
