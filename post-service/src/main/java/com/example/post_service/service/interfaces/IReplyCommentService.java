package com.example.post_service.service.interfaces;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.request.ReplyCommentRequest;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface IReplyCommentService {
    List<CommentResponse> getAllReplyCommentsByCommentId(Long commentId, String token);
    Comment createReplyComment(Long commentId, ReplyCommentRequest request, String userId, HttpServletRequest httpRequest);
    CommentResponse updateReplyComment(Long parentCommentId, String userId, CommentRequest request, HttpServletRequest httpRequest);
    void deleteReplyComment(Long parentCommentId, String userId, HttpServletRequest httpRequest);
}
