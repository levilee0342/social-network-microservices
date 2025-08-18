package com.example.post_service.service.interfaces;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ICommentService {
    Comment createComment(Long postId, CommentRequest request, String userId, HttpServletRequest httpRequest);
    CommentResponse updateComment(Long commentId, String userId, CommentRequest request, HttpServletRequest httpRequest);
    void deleteComment(Long commentId, String userId, HttpServletRequest httpRequest);
    List<CommentResponse> getAllCommentsByPostId(Long postId, String token);
}