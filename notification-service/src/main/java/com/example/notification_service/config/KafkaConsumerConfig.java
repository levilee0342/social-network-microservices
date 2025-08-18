package com.example.notification_service.config;

import com.example.notification_service.dto.request.FriendNotificationRequest;
import com.example.notification_service.dto.request.NotificationRequest;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    private Map<String, Object> commonConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "presence-group");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
        return props;
    }

    @Bean
    public ConsumerFactory<String, NotificationRequest> notificationConsumerFactory() {
        JsonDeserializer<NotificationRequest> deserializer = new JsonDeserializer<>(NotificationRequest.class, false);
        return new DefaultKafkaConsumerFactory<>(commonConfigs(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationRequest> notificationKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(notificationConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, FriendNotificationRequest> friendNotificationConsumerFactory() {
        JsonDeserializer<FriendNotificationRequest> deserializer = new JsonDeserializer<>(FriendNotificationRequest.class, false);
        return new DefaultKafkaConsumerFactory<>(commonConfigs(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, FriendNotificationRequest> friendNotificationKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FriendNotificationRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(friendNotificationConsumerFactory());
        return factory;
    }
}
