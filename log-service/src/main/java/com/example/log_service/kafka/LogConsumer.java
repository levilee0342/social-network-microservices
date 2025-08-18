package com.example.log_service.kafka;

import com.example.log_service.entity.Log;
import com.example.log_service.service.LogService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LogConsumer {
    private static final Logger logger = LoggerFactory.getLogger(LogConsumer.class);

    @Autowired
    private LogService logService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "logs-topic", groupId = "logging-group")
    public void consumeLog(ConsumerRecord<String, Log> record, Acknowledgment acknowledgment) {
        try {
            Log log = record.value();
            if (log != null) {
                Log savedLog = logService.saveLog(log);
                messagingTemplate.convertAndSend("/topic/logs", savedLog);
            } else {
                logger.error("Received null log due to deserialization error for record: {}", record);
            }
            acknowledgment.acknowledge();
        } catch (Exception e) {
            logger.error("Error processing record: {}", record, e);
        }
    }
}