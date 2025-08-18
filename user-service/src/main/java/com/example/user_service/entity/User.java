package com.example.user_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

@Data
@Document
public class User {
    @Id
    String userId;
    String email;
    String password;
}
