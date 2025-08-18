package com.example.message_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

@Data
@Document
public class BlockMessageDocument {
    @Id
    private String id;
    private String blockerId;
    private String blockedId;
    private Long blockedAt;
}
