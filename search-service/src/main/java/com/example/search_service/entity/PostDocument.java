package com.example.search_service.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document
@Data
public class PostDocument {
    @Id
    private String postId;
    private String content;
    private String userId;
    private List<String> hashtags;
    private String visibility; // PUBLIC, FRIENDS, PRIVATE
    private Long createdAt;
    private String type = "post";
}
