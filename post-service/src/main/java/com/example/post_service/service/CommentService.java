package com.example.post_service.service;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.request.ReplyCommentRequest;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import com.example.post_service.service.interfaces.ICommentPostShareService;
import com.example.post_service.service.interfaces.ICommentService;
import com.example.post_service.service.interfaces.IReplyCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final ICommentService commentService;
    private final IReplyCommentService replyCommentService;
    private final ICommentPostShareService commentPostShareService;

    public CommentService(ICommentService commentService,
                          IReplyCommentService replyCommentService,
                          ICommentPostShareService commentPostShareService){
        this.commentService = commentService;
        this.replyCommentService = replyCommentService;
        this.commentPostShareService = commentPostShareService;
    }

    // Comment
    public List<CommentResponse> getAllCommentsByPostId(Long postId, String token){
        return commentService.getAllCommentsByPostId(postId, token);
    }

    public Comment createComment(Long postId, CommentRequest request, String userId, HttpServletRequest httpRequest) {
        return commentService.createComment(postId, request, userId, httpRequest);
    }

    public CommentResponse updateComment(Long commentId, String userId, CommentRequest request, HttpServletRequest httpRequest) {
        return commentService.updateComment(commentId, userId, request, httpRequest);
    }

    public void deleteComment(Long commentId, String userId, HttpServletRequest httpRequest) {
        commentService.deleteComment(commentId, userId, httpRequest);
    }

    // Reply Comment
    public List<CommentResponse> getAllReplyCommentsByCommentId(Long commentId, String token) {
        return replyCommentService.getAllReplyCommentsByCommentId(commentId, token);
    }

    public Comment createReplyComment(Long commentId, ReplyCommentRequest request, String userId, HttpServletRequest httpRequest){
        return replyCommentService.createReplyComment(commentId, request, userId, httpRequest);
    }

    public CommentResponse updateReplyComment(Long parentCommentId, String userId, CommentRequest request, HttpServletRequest httpRequest){
        return replyCommentService.updateReplyComment(parentCommentId, userId, request, httpRequest);
    }

    public void deleteReplyComment(Long parentCommentId, String userId, HttpServletRequest httpRequest) {
        replyCommentService.deleteReplyComment(parentCommentId, userId, httpRequest);
    }

    //Comment
    public List<CommentResponse> getAllCommentsByShareId(Long shareId, String token) {
        return commentPostShareService.getAllCommentsByShareId(shareId, token);
    }

    public Comment createPostShareComment(Long shareId, CommentRequest request, String userId, HttpServletRequest httpRequest){
        return commentPostShareService.createPostShareComment(shareId, request, userId, httpRequest);
    }

    public CommentResponse updatePostShareComment(Long commentId, String userId, CommentRequest request, HttpServletRequest httpRequest) {
        return commentPostShareService.updatePostShareComment(commentId, userId, request, httpRequest);
    }

    public void deletePostShareComment(Long commentId, String userId, HttpServletRequest httpRequest) {
        commentPostShareService.deletePostShareComment(commentId, userId, httpRequest);
    }
}
