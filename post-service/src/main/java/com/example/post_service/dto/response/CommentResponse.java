package com.example.post_service.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private Long shareId;
    private String userId;
    private String authorName;
    private String authorAvatarUrl;
    private String content;
    private String imageUrl;
    private int reactionCount;
    private List<CommentResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
