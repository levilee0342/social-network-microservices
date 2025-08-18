package com.example.post_service.service;

import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.interfaces.IPostBookmarkService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostBookMarkService {

    private final IPostBookmarkService postBookmarkService;

    public PostBookMarkService(IPostBookmarkService postBookmarkService) {
        this.postBookmarkService = postBookmarkService;
    }

    public List<PostResponse> getBookmarkPostsByUser(String userId, HttpServletRequest httpRequest){
        return postBookmarkService.getBookmarkPostsByUser(userId, httpRequest);
    }
    public void BookmarkPost(String userId, Long postId, HttpServletRequest httpRequest){
        postBookmarkService.BookmarkPost(userId, postId, httpRequest);
    }
}
