package com.example.post_service.dto.response;

import com.example.post_service.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long postId;
    private String userId;
    private String authorName;
    private String authorAvatarUrl;
    private String title;
    private String content;
    private String voiceUrl;
    private Integer voiceDuration;
    private List<String> imageUrls;
    private List<String> hashtags;
    private Post.PostVisibility visibility;
    private Long reactionCount;
    private List<CommentResponse> comments;
    private Long typeId;
    private Long createdAt;
    private Long updatedAt;
    // dành riêng cho postShare
    private Long shareId;
    private Long shareCreateAt;
}
