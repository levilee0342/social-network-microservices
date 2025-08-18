package com.example.post_service.controller;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.request.ReplyCommentRequest;
import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import com.example.post_service.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    //Comment Post
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long postId, HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        List<CommentResponse> comments = commentService.getAllCommentsByPostId(postId, token);
        return ResponseEntity.ok(ApiResponse.<List<CommentResponse>>builder()
                .code(200)
                .message("Get comments successfully")
                .result(comments)
                .build());
    }

    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<Comment>> createComment(@PathVariable Long postId, @RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        Comment response = commentService.createComment(postId, request, userId, httpRequest);
        ApiResponse<Comment> apiResponse = ApiResponse.<Comment>builder()
                .code(200)
                .message("Comment created successfully")
                .result(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(@PathVariable Long commentId, @RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        CommentResponse response = commentService.updateComment(commentId, userId, request, httpRequest);
        ApiResponse<CommentResponse> apiResponse = ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("Comment updated successfully")
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        commentService.deleteComment(commentId, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Comment deleted successfully")
                .result(null)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    // Reply comment
    @GetMapping("/reply/{commentId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplyComments(@PathVariable Long commentId, HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        List<CommentResponse> parentComments = commentService.getAllReplyCommentsByCommentId(commentId, token);
        return ResponseEntity.ok(ApiResponse.<List<CommentResponse>>builder()
                .code(200)
                .message("Get reply comments successfully")
                .result(parentComments)
                .build());
    }

    @PostMapping("/reply/{commentId}")
    public ResponseEntity<ApiResponse<Comment>> createReplyComment(@PathVariable Long commentId, @RequestBody ReplyCommentRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        Comment response = commentService.createReplyComment(commentId, request, userId, httpRequest);
        ApiResponse<Comment> apiResponse = ApiResponse.<Comment>builder()
                .code(200)
                .message("Reply comment created successfully")
                .result(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/reply/{parentCommentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateReplyComment(@PathVariable Long parentCommentId, @RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        CommentResponse response = commentService.updateReplyComment(parentCommentId, userId, request, httpRequest);
        ApiResponse<CommentResponse> apiResponse = ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("Reply Comment updated successfully")
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/reply/{parentCommentId}")
    public ResponseEntity<ApiResponse<Void>> deleteReplyComment(@PathVariable Long parentCommentId, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        commentService.deleteReplyComment(parentCommentId, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Reply Comment deleted successfully")
                .result(null)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    //Comment Post Share
    @GetMapping("/shares/{commentId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getPostShareComments(@PathVariable Long commentId, HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        List<CommentResponse> parentComments = commentService.getAllCommentsByShareId(commentId, token);
        return ResponseEntity.ok(ApiResponse.<List<CommentResponse>>builder()
                .code(200)
                .message("Get post share comments successfully")
                .result(parentComments)
                .build());
    }

    @PostMapping("/shares/{commentId}")
    public ResponseEntity<ApiResponse<Comment>> createPostShareComment(@PathVariable Long commentId, @RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        Comment response = commentService.createPostShareComment(commentId, request, userId, httpRequest);
        ApiResponse<Comment> apiResponse = ApiResponse.<Comment>builder()
                .code(200)
                .message("Post share comment created successfully")
                .result(response)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/shares/{parentCommentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updatePostShareComment(@PathVariable Long parentCommentId, @RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        CommentResponse response = commentService.updatePostShareComment(parentCommentId, userId, request, httpRequest);
        ApiResponse<CommentResponse> apiResponse = ApiResponse.<CommentResponse>builder()
                .code(200)
                .message("Post share Comment updated successfully")
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/shares/{parentCommentId}")
    public ResponseEntity<ApiResponse<Void>> deletePostShareComment(@PathVariable Long parentCommentId, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        commentService.deletePostShareComment(parentCommentId, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Post share Comment deleted successfully")
                .result(null)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }
}
