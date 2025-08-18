package com.example.search_service.dto.response;

import com.example.search_service.entity.PostVisibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private String postId;
    private String userId;
    private String authorName;
    private String authorAvatarUrl;
    private String title;
    private String content;
    private String voiceUrl;
    private Integer voiceDuration;
    private List<String> imageUrls;
    private List<String> hashtags;
    private PostVisibility visibility;
    private Long reactionCount;
    private List<CommentResponse> comments;
    private Long typeId;
    private Long createdAt;
    private Long updatedAt;
}
