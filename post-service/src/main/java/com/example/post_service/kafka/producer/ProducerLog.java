package com.example.post_service.kafka.producer;

import com.example.post_service.entity.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ProducerLog {
    private static final String TOPIC = "logs-topic";

    private final KafkaTemplate<String, Log> kafkaTemplate;

    public ProducerLog(@Qualifier("logKafkaTemplate") KafkaTemplate<String, Log> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLog(String serviceName, String logLevel, String message) {
        Log log = new Log();
        log.setServiceName(serviceName);
        log.setLogLevel(logLevel);
        log.setMessage(message);
        log.setTimestamp(LocalDateTime.now());
        kafkaTemplate.send(TOPIC, serviceName, log);
    }
}

