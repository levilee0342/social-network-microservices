package com.example.message_service.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMarkReadRequest {
    private String messageId;
    private String conversationId;
}