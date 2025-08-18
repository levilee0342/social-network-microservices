package com.example.api_gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "request_log")
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "method")
    private String method;

    @Column(name = "path")
    private String path;

    @Column(name = "status")
    private int status;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "response_time")
    private int responseTime;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;
}
