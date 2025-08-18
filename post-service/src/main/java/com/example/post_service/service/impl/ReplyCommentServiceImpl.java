package com.example.post_service.service.impl;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.request.ReplyCommentRequest;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerPostEvent;
import com.example.post_service.repository.CommentRepository;
import com.example.post_service.service.interfaces.ICommentMapperService;
import com.example.post_service.service.interfaces.IReplyCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReplyCommentServiceImpl implements IReplyCommentService {

    private final CommentRepository commentRepository;
    private final ICommentMapperService commentMapperService;
    private final ProducerNotification producerNotification;
    private final ProducerPostEvent producerEvent;
    private final ProducerLog producerLog;

    public ReplyCommentServiceImpl(CommentRepository commentRepository,
                                   ICommentMapperService commentMapperService,
                                   ProducerNotification producerNotification,
                                   ProducerPostEvent producerEvent,
                                   ProducerLog producerLog) {
        this.commentRepository = commentRepository;
        this.commentMapperService = commentMapperService;
        this.producerNotification = producerNotification;
        this.producerEvent = producerEvent;
        this.producerLog = producerLog;
    }

    @Override
    @Transactional
    public List<CommentResponse> getAllReplyCommentsByCommentId(Long commentId, String token) {
        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            return commentRepository.findByParentCommentOrderByCreatedAtDesc(comment).stream()
                    .map(commentMapperService::mapToCommentResponse)
                    .toList();
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reply Comment] Failed to get all reply comments by comment: " + commentId + ". Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_GET_REPLY_COMMENT);
        }
    }

    @Override
    @Transactional
    public Comment createReplyComment(Long commentId, ReplyCommentRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Optional<Comment> commentOpt = commentRepository.findById(commentId);
            if (commentOpt.isEmpty()) {
                throw new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS);
            }
            Comment comment = commentOpt.get();
            Comment parentComment = new Comment();
            parentComment.setParentComment(comment);
            parentComment.setUserId(userId);
            parentComment.setContent(request.getContent());
            Comment savedComment = commentRepository.save(parentComment);
            producerEvent.publishCommentEvent(savedComment, PostEventType.COMMENT_REPLY_ADDED, userId, httpRequest);
            producerNotification.sendNotification("NEW_REPLY_COMMENT", userId, String.valueOf(comment.getCommentId()), commentId, token);
            return savedComment;
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to create reply comments. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_CREATE_REPLY_COMMENT);
        }
    }

    @Override
    @Transactional
    public CommentResponse updateReplyComment(Long parentCommentId, String userId, CommentRequest request, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Comment parentcomment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            if (!parentcomment.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.ACCESS_NOT_EDIT_COMMENT);
            }
            Comment oldCommentCopy = new Comment();
            oldCommentCopy.setCommentId(parentcomment.getCommentId());
            oldCommentCopy.setPost(parentcomment.getPost());
            oldCommentCopy.setUserId(parentcomment.getUserId());
            oldCommentCopy.setContent(parentcomment.getContent());
            parentcomment.setContent(request.getContent());
            Comment updatedComment = commentRepository.save(parentcomment);
            producerEvent.publishCommentEvent(updatedComment, PostEventType.COMMENT_REPLY_UPDATED, userId, httpRequest);
            Comment comment = commentRepository.findById(updatedComment.getParentComment().getCommentId())
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            producerNotification.sendNotification("REP_COMMENT_UPDATED", userId, comment.getUserId(), updatedComment.getParentComment().getCommentId(), token);
            return commentMapperService.mapToCommentResponse(updatedComment);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to update reply comments. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_UPDATE_REPLY_COMMENT);
        }
    }

    @Override
    @Transactional
    public void deleteReplyComment(Long parentCommentId, String userId, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            if (!parentComment.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.ACCESS_NOT_DELETE_COMMENT);
            }
            producerEvent.publishCommentEvent(parentComment, PostEventType.COMMENT_REPLY_DELETED, userId, httpRequest);
            Comment comment = commentRepository.findById(parentComment.getParentComment().getCommentId())
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            producerNotification.sendNotification("REP_COMMENT_DELETED", userId, comment.getUserId(), parentComment.getParentComment().getCommentId(), token);
            commentRepository.delete(comment);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to delete reply comments. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_DELETE_COMMENT);
        }
    }
}
