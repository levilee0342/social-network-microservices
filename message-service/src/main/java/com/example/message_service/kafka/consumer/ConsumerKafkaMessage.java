package com.example.message_service.kafka.consumer;

import com.example.message_service.controller.WebSocketMessageController;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.error.AuthErrorCode;
import com.example.message_service.exception.AppException;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.repository.ICouchbaseMessageLogRepository;
import com.example.message_service.service.interfaces.IMessageDispatcherService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ConsumerKafkaMessage {

    private final ICouchbaseMessageLogRepository messageLogRepository;
    private final IMessageDispatcherService messageDispatcherService;
    private final ProducerLog producerLog;

    public ConsumerKafkaMessage(ICouchbaseMessageLogRepository messageLogRepository,
                                IMessageDispatcherService messageDispatcherService,
                                ProducerLog producerLog){
        this.messageLogRepository = messageLogRepository;
        this.messageDispatcherService = messageDispatcherService;
        this.producerLog = producerLog;
    }

    @KafkaListener(topics = "messages", groupId = "message-group")
    public void listenMessage(MessageDocument message) {
        try {
            messageLogRepository.saveMessageLog(message);
            messageDispatcherService.dispatchMessage(message);
            producerLog.sendLog(
                    "message-service",
                    "INFO",
                    String.format("[KafkaReceiver] Successfully processed message id=%s",
                            message.getIdMessage())
            );
        } catch (Exception e) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    String.format("[KafkaReceiver] Failed to process message id=%s. Reason: %s",
                            message.getIdMessage(), e.getMessage())
            );
            throw new AppException(AuthErrorCode.PROCESS_MESS_FAILED);
        }
    }
}
