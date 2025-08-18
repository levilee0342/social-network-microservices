package com.example.notification_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Data
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "email_notifications")
    private boolean emailNotifications = true;

    @Column(name = "push_notifications")
    private boolean pushNotifications = true;

    @Column(name = "in_app_notifications")
    private boolean inAppNotifications = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
