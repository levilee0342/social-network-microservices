package com.example.message_service.dto.request;

import lombok.Data;

@Data
public class ChatFetchMessagesRequest {
    private String conversationId;
}
