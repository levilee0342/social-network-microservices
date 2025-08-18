package com.example.presence_service.controller;

import com.example.presence_service.entity.PresenceEvent;
import com.example.presence_service.entity.PresenceEventType;
import com.example.presence_service.scheduler.WebSocketHeartbeatScheduler;
import com.example.presence_service.service.interfaces.IPresenceEventHandlerService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class PresenceMessageController {

    private final IPresenceEventHandlerService handlerService;
    private final WebSocketHeartbeatScheduler heartbeatScheduler;

    public PresenceMessageController(IPresenceEventHandlerService handlerService, WebSocketHeartbeatScheduler heartbeatScheduler) {
        this.handlerService = handlerService;
        this.heartbeatScheduler = heartbeatScheduler;
    }

    @MessageMapping("/presence")
    public void onMessage(@Payload PresenceEvent event) {
        if (event.getEventType() == PresenceEventType.PONG) {
            heartbeatScheduler.recordPongResponse(event.getUserId());
        } else {
            handlerService.handleEvent(event);
        }
    }
}

