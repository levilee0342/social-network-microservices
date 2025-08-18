package com.example.message_service.config;

import com.example.message_service.dto.request.AnonymousChatRequest;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.entity.PresenceEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, MessageDocument> messageConsumerFactory(KafkaProperties kafkaProperties) {
        KafkaProperties.Consumer consumerProps = kafkaProperties.getConsumer();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProps.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Optional: set auto-offset-reset if defined
        if (consumerProps.getAutoOffsetReset() != null) {
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProps.getAutoOffsetReset());
        }

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(MessageDocument.class)
        );
    }

    @Bean
    public ConsumerFactory<String, PresenceEvent> presenceConsumerFactory(KafkaProperties kafkaProperties) {
        KafkaProperties.Consumer consumerProps = kafkaProperties.getConsumer();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProps.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Các tuỳ chọn khác nếu cần
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProps.getAutoOffsetReset());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(PresenceEvent.class)
        );
    }

    @Bean
    public ConsumerFactory<String, AnonymousChatRequest> anonymousChatConsumerFactory(KafkaProperties kafkaProperties) {
        KafkaProperties.Consumer consumerProps = kafkaProperties.getConsumer();

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProps.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Các tuỳ chọn khác nếu cần
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerProps.getAutoOffsetReset());

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JsonDeserializer<>(AnonymousChatRequest.class)
        );
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageDocument> kafkaListenerContainerFactory(
            ConsumerFactory<String, MessageDocument> messageConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, MessageDocument> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(messageConsumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PresenceEvent> presenceEventKafkaListenerContainerFactory(
            ConsumerFactory<String, PresenceEvent> presenceConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PresenceEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(presenceConsumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AnonymousChatRequest> anonymousChatKafkaListenerContainerFactory(
            ConsumerFactory<String, AnonymousChatRequest> anonymousChatConsumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, AnonymousChatRequest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(anonymousChatConsumerFactory);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(1000L, 3)));
        return factory;
    }
}
