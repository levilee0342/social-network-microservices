package com.example.message_service.service.impl;

import com.example.message_service.controller.WebSocketMessageController;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.repository.ICouchbaseConversationRepository;
import com.example.message_service.service.interfaces.IMessageDispatcherService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageDispatcherServiceImpl implements IMessageDispatcherService {

    private final WebSocketMessageController socketController;
    private final ICouchbaseConversationRepository couchbaseConversationRepository;

    public MessageDispatcherServiceImpl(WebSocketMessageController socketController,
                                        ICouchbaseConversationRepository couchbaseConversationRepository) {
        this.socketController = socketController;
        this.couchbaseConversationRepository = couchbaseConversationRepository;
    }

    @Override
    public void dispatchMessage(MessageDocument message) {
        List<String> receiverIds = couchbaseConversationRepository.getParticipants(message.getConversationId());
        for (String receiverId : receiverIds) {
            if (!receiverId.equals(message.getSenderId())) {
                socketController.sendMessageToUser(receiverId, message);
            }
        }
    }
}
