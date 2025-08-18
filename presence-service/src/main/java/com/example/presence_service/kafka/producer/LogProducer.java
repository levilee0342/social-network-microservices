package com.example.presence_service.kafka.producer;

import com.example.presence_service.entity.Log;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LogProducer {
    private static final String TOPIC = "logs-topic";

    private final KafkaTemplate<String, Log> kafkaTemplate;

    // Inject đúng bean bằng @Qualifier
    public LogProducer(@Qualifier("logKafkaTemplate") KafkaTemplate<String, Log> kafkaTemplate) {
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
