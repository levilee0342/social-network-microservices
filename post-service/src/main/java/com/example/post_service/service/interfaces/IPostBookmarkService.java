package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.PostResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IPostBookmarkService {
    List<PostResponse> getBookmarkPostsByUser(String userId, HttpServletRequest httpRequest);
    void BookmarkPost(String userId, Long postId, HttpServletRequest httpRequest);
}
