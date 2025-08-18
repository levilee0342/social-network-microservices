package com.example.post_service.service;

import com.example.post_service.dto.response.PostResponse;
import com.example.post_service.service.interfaces.IPostShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostShareService {

    private final IPostShareService postShareService;

    public PostShareService(IPostShareService postShareService) {
        this.postShareService = postShareService;
    }

    public List<PostResponse> getSharedPostsByUser(String userId, HttpServletRequest httpRequest){
        return postShareService.getSharedPostsByUser(userId, httpRequest);
    }

    public void sharePost(String userId, Long postId, String content, HttpServletRequest httpRequest){
        postShareService.sharePost(userId, postId, content, httpRequest);
    }
}
