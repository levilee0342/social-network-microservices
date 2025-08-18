package com.example.message_service.dto.response;

import java.util.List;
import com.example.message_service.entity.MessageDocument;
import lombok.Data;

@Data
public class ChatFetchMessagesResponse {
    private String type;
    private String conversationId;
    private List<MessageDocument> messages;
}
