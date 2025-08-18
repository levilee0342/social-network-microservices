package com.example.post_service.service.interfaces;

import com.example.post_service.enums.ReactionType;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface IReactionPostShareService {
    void reactToPostShare(Long shareId, ReactionType type, String userId, HttpServletRequest request);
    void unreactToPostShare(Long shareId, ReactionType type, String userId, HttpServletRequest request);
    long countReactionsByShareId(Long postId);
    Map<String, Integer> countReactionsPostShareGroupByType(Long shareId);
}
