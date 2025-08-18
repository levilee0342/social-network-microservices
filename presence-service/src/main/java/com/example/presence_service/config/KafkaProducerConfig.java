package com.example.presence_service.config;

import com.example.presence_service.entity.Log;
import com.example.presence_service.entity.PresenceEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    private Map<String, Object> commonProducerConfigs() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return configProps;
    }

    private <T> ProducerFactory<String, T> createProducerFactory() {
        return new DefaultKafkaProducerFactory<>(commonProducerConfigs());
    }

    private <T> KafkaTemplate<String, T> createKafkaTemplate(ProducerFactory<String, T> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, PresenceEvent> presenceEventKafkaTemplate() {
        return createKafkaTemplate(createProducerFactory());
    }

    @Bean(name = "logKafkaTemplate")
    public KafkaTemplate<String, Log> logKafkaTemplate() {
        return createKafkaTemplate(createProducerFactory());
    }

}
