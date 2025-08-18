package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.entity.Post;

public interface IPostMapperService {
    PostResponse mapToPostResponse(Post post);
    PostResponse mapToPostShareResponse(Post post, Long shareId, Long sharedAt);
}
