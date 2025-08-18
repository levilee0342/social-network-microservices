package com.example.log_service.config;

import com.example.log_service.entity.Log;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class ConsumerConfig {

    @Bean
    public ConsumerFactory<String, Log> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "logging-group");
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", ErrorHandlingDeserializer.class.getName());
        props.put("value.deserializer", ErrorHandlingDeserializer.class.getName());
        props.put("spring.deserializer.key.delegate.class", StringDeserializer.class.getName());
        props.put("spring.deserializer.value.delegate.class", JsonDeserializer.class.getName());
        props.put("spring.json.trusted.packages", "com.example.log_service.entity,com.example.user_service.entity");
        props.put("spring.json.value.default.type", Log.class.getName());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new ErrorHandlingDeserializer<>(new StringDeserializer()),
                new ErrorHandlingDeserializer<>(new JsonDeserializer<>(Log.class, false))
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Log> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Log> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(AckMode.MANUAL); // Thêm cấu hình AckMode
        factory.setCommonErrorHandler(new DefaultErrorHandler());
        return factory;
    }
}