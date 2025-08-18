package com.example.notification_service.repository;

import com.example.notification_service.entity.Notification;
import com.example.notification_service.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdAndRelatedEntityType(String userId, String relatedEntityType, Pageable pageable);
    Page<Notification> findByUserIdAndIsReadAndRelatedEntityType(String userId, boolean isRead, String relatedEntityType, Pageable pageable);
    List<Notification> findAllById(Iterable<Long> ids);

    Optional<Notification> findFirstByUserIdAndTypeAndRelatedEntityIdAndRelatedEntityTypeAndIsReadFalseOrderByCreatedAtDesc(
            String userId,
            NotificationType type,
            String relatedEntityId,
            String relatedEntityType
    );

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(String userId, boolean isRead, Pageable pageable);
}