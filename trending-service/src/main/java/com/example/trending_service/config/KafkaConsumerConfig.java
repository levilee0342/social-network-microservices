package com.example.trending_service.config;

import com.example.trending_service.dto.request.TrendingEventRequest;
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
        props.put("group.id", "trending-group");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.example.trending_service.dto.request.TrendingEventRequest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.example.trending_service.dto.request");
        return props;
    }

    @Bean
    public ConsumerFactory<String, TrendingEventRequest> TrendingConsumerFactory() {
        JsonDeserializer<TrendingEventRequest> deserializer = new JsonDeserializer<>(TrendingEventRequest.class, false);
        return new DefaultKafkaConsumerFactory<>(commonConfigs(), new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TrendingEventRequest> TrendingKafkaListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TrendingEventRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(TrendingConsumerFactory());
        return factory;
    }
}
