package com.example.message_service.kafka.consumer;
import com.example.message_service.entity.PresenceEvent;
import com.example.message_service.entity.UserPresence;
import com.example.message_service.repository.IRedisUserPresenceRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerPresenceEvent {

    private final IRedisUserPresenceRedisRepository presenceRedisRepository;

    @KafkaListener(
            topics = "presence-events",
            groupId = "message-service-group",
            containerFactory = "presenceEventKafkaListenerContainerFactory"
    )
    public void consumePresenceEvent(PresenceEvent event) {
        if (event.getUserId() == null || event.getEventType() == null) {
            log.warn("[PresenceEventConsumer] Invalid event received, skipping...");
            return;
        }

        UserPresence userPresence = UserPresence.builder()
                .userId(event.getUserId())
                .status(event.getEventType())
                .roomId(event.getRoomId())
                .lastActive(Instant.now().toEpochMilli())
                .lastOnlineTimestamp(Instant.now().toEpochMilli())
                .build();

        presenceRedisRepository.save(userPresence);
    }
}

