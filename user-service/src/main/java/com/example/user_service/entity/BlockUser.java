package com.example.user_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document
public class BlockUser {
    @Id
    private String id;             // VD: "block::<blockerId>::<blockedId>"
    private String blockerId;
    private String blockedId;
    private Long blockedAt;
}
