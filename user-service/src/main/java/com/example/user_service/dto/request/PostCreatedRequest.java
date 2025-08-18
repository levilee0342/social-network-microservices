package com.example.user_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCreatedRequest {
    private String userId;
    private Long postId;
    private Long createdAt;

}
