package com.example.post_service.service.interfaces;

import com.example.post_service.enums.ReactionType;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface IReactionService {
    void reactToPost(Long postId, ReactionType type, String userId, HttpServletRequest request);
    void unreactToPost(Long postId, ReactionType type, String userId, HttpServletRequest request);
    long countReactionsByPostId(Long postId);
    Map<String, Object> getReactionDetails(Long postId);
}