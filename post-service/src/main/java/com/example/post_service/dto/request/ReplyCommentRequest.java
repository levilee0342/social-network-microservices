package com.example.post_service.dto.request;

import lombok.Data;

@Data
public class ReplyCommentRequest {
    Long commentId;
    String userId;
    String content;
    String imageUrl;
}
