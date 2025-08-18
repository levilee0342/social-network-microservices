package com.example.user_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

@Data
@Document
public class Friendship {
    @Id
    private String friendshipId;
    private String userId1;
    private String userId2;
    private long createdAt;
}
