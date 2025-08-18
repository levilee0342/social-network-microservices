package com.example.presence_service.service.interfaces;

import com.example.presence_service.entity.PresenceEvent;

public interface IPresenceEventHandlerService {
    void handleEvent(PresenceEvent event);
}
