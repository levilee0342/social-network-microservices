package com.example.post_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Jacksonized
public class SearchPostIndex {
    private Long postId;
    private String userId;
    private String title;
    private String content;
    private List<String> hashtags;
    private Long createdAt;
    private String visibility;
}
