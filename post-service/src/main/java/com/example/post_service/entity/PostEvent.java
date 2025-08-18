package com.example.post_service.entity;

import com.example.post_service.enums.PostEventType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_events")
@Data
@EntityListeners(AuditingEntityListener.class)
public class PostEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostEventType eventType;

    @Column(columnDefinition = "TEXT")
    private String eventData;

    @Column(columnDefinition = "TEXT")
    private String previousData;

    //Theo lỗi IP lạ => bảo mật hệ thống
    private String ipAddress;
    //Thông tin thiết bị
    private String userAgent;
    //Lý do thực hiện hành động
    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
