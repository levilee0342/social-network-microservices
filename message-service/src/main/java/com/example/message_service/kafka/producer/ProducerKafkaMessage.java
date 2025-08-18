package com.example.message_service.kafka.producer;

import com.example.message_service.entity.MessageDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProducerKafkaMessage{

    private final KafkaTemplate<String, MessageDocument> kafkaTemplate;
    private final ProducerLog producerLog;

    public ProducerKafkaMessage(KafkaTemplate<String, MessageDocument> kafkaTemplate, ProducerLog producerLog){
        this.kafkaTemplate = kafkaTemplate;
        this.producerLog = producerLog;
    }

    public void publishMessage(MessageDocument message) {
        try {
            kafkaTemplate.send("messages", message.getConversationId(), message);
        } catch (Exception e) {
            log.error("Failed to publish message {} to Kafka", message.getIdMessage(), e);
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    String.format("[KafkaPublisher] Failed to publish message %s to kafka: %s",
                            message.getIdMessage(), e)
            );
        }
    }
}
