package com.example.message_service.kafka.producer;

import com.example.message_service.dto.request.AnonymousChatRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProducerAnonymousChatRequest {

    private final KafkaTemplate<String, AnonymousChatRequest> kafkaTemplate;
    private final ProducerLog producerLog;

    public ProducerAnonymousChatRequest(KafkaTemplate<String, AnonymousChatRequest> kafkaTemplate,
                                        ProducerLog producerLog) {
        this.kafkaTemplate = kafkaTemplate;
        this.producerLog = producerLog;
    }

    public void sendAnonymousChatRequest(AnonymousChatRequest request) {
        kafkaTemplate.send("anonymous-chat-requests", request.getUserId(), request);
        System.out.println("send anonymous chat request");
        producerLog.sendLog(
                "message-service",
                "DEBUG",
                "[KafkaProducer] Sent anonymous chat request for user " + request.getUserId() + " to topic anonymous-chat-requests");
    }
}
