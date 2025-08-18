package com.example.job_system_social.detect_spam_comment;

import com.example.job_system_social.model.BlockUserEvent;
import com.example.job_system_social.model.NotificationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class BlockUserKafkaProducer {
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BlockUserKafkaProducer(Properties props) {
        this.producer = new KafkaProducer<>(props);
    }

    public void send(BlockUserEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            ProducerRecord<String, String> record = new ProducerRecord<>("user-block-events", event.getUserId(), json);
            producer.send(record);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void sendBlockUserNotification(String userId) {
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setEventType("BLOCK_USER");
            notification.setUserId(userId);
            notification.setOwnerId("SYSTEM");
            notification.setPostId(null);
            notification.setToken(null);

            String json = objectMapper.writeValueAsString(notification);
            ProducerRecord<String, String> record = new ProducerRecord<>("notification-events", userId, json);
            producer.send(record);
            System.out.println("🚨 Sent BLOCK_USER notification to Kafka: " + json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}

