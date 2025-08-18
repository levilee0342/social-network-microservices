package com.example.message_service.service.interfaces;

import com.example.message_service.entity.MessageDocument;

public interface IMessageDispatcherService {
    void dispatchMessage(MessageDocument message);
}
