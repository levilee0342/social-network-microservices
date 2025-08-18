package com.example.presence_service.config;

import com.example.presence_service.entity.EventPresence;
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
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.presence_service.entity.EventPresence");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.presence_service.entity");
        return props;
    }

    @Bean
    public ConsumerFactory<String, EventPresence> PresenceConsumerFactory() {
        JsonDeserializer<EventPresence> deserializer = new JsonDeserializer<>(EventPresence.class, false);
        return new DefaultKafkaConsumerFactory<>(commonConfigs(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventPresence> PresenceKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventPresence> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(PresenceConsumerFactory());
        return factory;
    }
}
