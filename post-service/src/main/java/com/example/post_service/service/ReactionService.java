package com.example.post_service.service;

import com.example.post_service.enums.ReactionType;
import com.example.post_service.service.interfaces.IReactionCommentService;
import com.example.post_service.service.interfaces.IReactionPostShareService;
import com.example.post_service.service.interfaces.IReactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReactionService {

    private final IReactionService reactionService;
    private final IReactionCommentService reactionCommentService;
    private final IReactionPostShareService reactionPostShareService;

    public ReactionService(IReactionService reactionService,
                           IReactionCommentService reactionCommentService,
                           IReactionPostShareService reactionPostShareService) {
        this.reactionService = reactionService;
        this.reactionCommentService = reactionCommentService;
        this.reactionPostShareService = reactionPostShareService;
    }

    // Reaction post
    public void reactToPost(Long postId, ReactionType type, String userId, HttpServletRequest httpRequest) {
        reactionService.reactToPost(postId, type, userId, httpRequest);
    }

    public void unreactToPost(Long postId, ReactionType type, String userId, HttpServletRequest request) {
        reactionService.unreactToPost(postId, type, userId, request);
    }

    public Map<String, Object> getReactionDetails(Long postId){
        return reactionService.getReactionDetails(postId);
    }

    //Reaction Comment
    public void reactToComment(Long commentId, ReactionType type, String userId, HttpServletRequest request){
        reactionCommentService.reactToComment(commentId, type, userId, request);
    }
    public void unreactToComment(Long commentId, ReactionType type, String userId, HttpServletRequest request){
        reactionCommentService.unreactToComment(commentId, type, userId, request);
    }

    public Map<String, Integer> countReactionsCommentGroupByType(Long commentId){
        return reactionCommentService.countReactionsCommentGroupByType(commentId);
    }

    //Reaction Post Share
    public void reactToPostShare(Long shareId, ReactionType type, String userId, HttpServletRequest request) {
        reactionPostShareService.reactToPostShare(shareId, type, userId, request);
    }

    public void unreactToPostShare(Long shareId, ReactionType type, String userId, HttpServletRequest request) {
        reactionPostShareService.unreactToPostShare(shareId, type, userId, request);
    }

    public Map<String, Integer> countReactionsPostShareGroupByType(Long shareId) {
        return reactionPostShareService.countReactionsPostShareGroupByType(shareId);
    }
}
