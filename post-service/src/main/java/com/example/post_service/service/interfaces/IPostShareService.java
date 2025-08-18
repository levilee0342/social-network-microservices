package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.PostResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IPostShareService {
    List<PostResponse> getSharedPostsByUser(String userId, HttpServletRequest httpRequest);
    void sharePost(String userId, Long postId, String content, HttpServletRequest httpRequest);
}
