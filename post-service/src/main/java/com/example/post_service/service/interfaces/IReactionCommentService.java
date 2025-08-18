package com.example.post_service.service.interfaces;

import com.example.post_service.enums.ReactionType;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface IReactionCommentService {
    void reactToComment(Long commentId, ReactionType type, String userId, HttpServletRequest request);
    void unreactToComment(Long commentId, ReactionType type, String userId, HttpServletRequest request);
    long countReactionsCommentByPostId(Long commentId);
    Map<String, Integer> countReactionsCommentGroupByType(Long commentId);
}
