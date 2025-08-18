package com.example.post_service.config;

import com.example.post_service.dto.request.*;
import com.example.post_service.entity.Log;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private Map<String, Object> commonProducerConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return configProps;
    }

    @Bean
    public ProducerFactory<String, PostEventRequest> postEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, PostEventRequest> postEventKafkaTemplate() {
        return new KafkaTemplate<>(postEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, NotificationRequest> notificationProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean
    public KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }

    @Bean(name = "logProducerFactory")
    public ProducerFactory<String, Log> logProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean(name = "logKafkaTemplate")
    public KafkaTemplate<String, Log> logKafkaTemplate(
            @Qualifier("logProducerFactory") ProducerFactory<String, Log> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    private Map<String, Object> userDailyProducerConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return configProps;
    }

    @Bean
    public ProducerFactory<String, PostCreatedRequest> postCreatedEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean(name = "postCreatedEventKafkaTemplate")
    public KafkaTemplate<String, PostCreatedRequest> postCreatedEventKafkaTemplate() {
        return new KafkaTemplate<>(postCreatedEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, UserInteractionEventRequest> userPreferenceEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean(name = "userPreferenceEventKafkaTemplate")
    public KafkaTemplate<String, UserInteractionEventRequest> userPreferenceEventKafkaTemplate() {
        return new KafkaTemplate<>(userPreferenceEventProducerFactory());
    }

    @Bean
    public ProducerFactory<String, CommentEventRequest> commentEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    @Bean(name = "commentEventKafkaTemplate")
    public KafkaTemplate<String, CommentEventRequest> commentEventKafkaTemplate() {
        return new KafkaTemplate<>(commentEventProducerFactory());
    }
}
