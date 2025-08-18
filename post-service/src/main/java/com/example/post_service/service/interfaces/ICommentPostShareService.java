package com.example.post_service.service.interfaces;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ICommentPostShareService {
    List<CommentResponse> getAllCommentsByShareId(Long shareId, String token);
    Comment createPostShareComment(Long shareId, CommentRequest request, String userId, HttpServletRequest httpRequest);
    CommentResponse updatePostShareComment(Long commentId, String userId, CommentRequest request, HttpServletRequest httpRequest);
    void deletePostShareComment(Long commentId, String userId, HttpServletRequest httpRequest);
}
