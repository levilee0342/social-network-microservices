package com.example.post_service.dto.request;

import com.example.post_service.entity.Post;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {
    private String title;
    private String content;
    private String voiceUrl;
    private Integer voiceDuration;
    private List<String> imageUrls;
    private List<String> hashtags;
    //private Boolean isPublic;
    private Post.PostVisibility visibility;
}
