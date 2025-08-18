package com.example.message_service.controller;

import com.example.message_service.entity.MessageDocument;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketMessageController {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessageController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendMessageToUser(String userId, MessageDocument message) {
        // Gửi đến user cụ thể (dùng queue nếu muốn private)
        messagingTemplate.convertAndSend("/topic/chat/" + userId, message);
    }

    // Nếu gửi broadcast
    public void sendBroadcastMessage(MessageDocument message) {
        messagingTemplate.convertAndSend("/topic/chat", message);
    }
}
