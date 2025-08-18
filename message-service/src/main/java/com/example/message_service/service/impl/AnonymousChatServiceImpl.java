package com.example.message_service.service.impl;

import com.example.message_service.dto.request.AnonymousChatRequest;
import com.example.message_service.enums.PresenceEventType;
import com.example.message_service.entity.UserPresence;
import com.example.message_service.error.NotExistedErrorCode;
import com.example.message_service.exception.AppException;
import com.example.message_service.kafka.producer.ProducerAnonymousChatRequest;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.IRedisUserPresenceRedisRepository;
import com.example.message_service.service.interfaces.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AnonymousChatServiceImpl implements IAnonymousChatService {

    private final IAuthManagerService authManagerService;
    private final IChatRequestValidatorService chatRequestValidatorService;
    private final IRedisUserPresenceRedisRepository IRedisUserPresenceRedisRepository;
    private final ProducerAnonymousChatRequest producerAnonymousChatRequest;
    private final ProducerLog producerLog;

    public AnonymousChatServiceImpl(IAuthManagerService authManagerService,
                                    IChatRequestValidatorService chatRequestValidatorService,
                                    IRedisUserPresenceRedisRepository IRedisUserPresenceRedisRepository,
                                    ProducerAnonymousChatRequest producerAnonymousChatRequest,
                                    ProducerLog producerLog) {
        this.authManagerService = authManagerService;
        this.chatRequestValidatorService = chatRequestValidatorService;
        this.IRedisUserPresenceRedisRepository = IRedisUserPresenceRedisRepository;
        this.producerAnonymousChatRequest = producerAnonymousChatRequest;
        this.producerLog = producerLog;
    }

    @Override
    public void requestAnonymousChat(AnonymousChatRequest request, HttpServletRequest httprequest) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!authManagerService.checkExitsUser(httprequest)) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    "[AnonymousChat] User not found: " + userId);
            throw new AppException(NotExistedErrorCode.USER_NOT_FOUND);
        }
        UserPresence presence = IRedisUserPresenceRedisRepository.findByUserId(userId);
        if (presence == null || presence.getStatus() != PresenceEventType.ONLINE) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    "[AnonymousChat] User " + userId + " is not online");
            throw new AppException(NotExistedErrorCode.USER_NOT_ONLINE);
        }
        chatRequestValidatorService.validate(request);
        request.setUserId(userId);
        request.setTimestamp(Instant.now().toEpochMilli());
        request.setMatched(false); // Đảm bảo trạng thái ban đầu là chưa matched
        // Gửi yêu cầu vào Kafka topic thông qua producer chuyên biệt
        producerAnonymousChatRequest.sendAnonymousChatRequest(request);
        producerLog.sendLog(
                "message-service",
                "DEBUG",
                "[AnonymousChat] Sent anonymous chat request for user " + userId + " to Kafka.");
    }
}
