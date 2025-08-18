package com.example.notification_service.entity;

import com.example.notification_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String content;

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private String relatedEntityId;

    private String relatedEntityType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_actors", joinColumns = @JoinColumn(name = "notification_id"))
    @Column(name = "actor_id")
    private List<String> actorIds = new ArrayList<>();
}

