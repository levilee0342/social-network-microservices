package com.example.post_service.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Log implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private String serviceName;
    private String logLevel;
    private String message;
    private LocalDateTime timestamp;
}
