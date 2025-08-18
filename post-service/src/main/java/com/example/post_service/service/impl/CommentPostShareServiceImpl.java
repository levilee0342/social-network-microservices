package com.example.post_service.service.impl;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import com.example.post_service.entity.PostShare;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerPostEvent;
import com.example.post_service.repository.CommentRepository;
import com.example.post_service.repository.PostShareRepository;
import com.example.post_service.service.interfaces.ICommentMapperService;
import com.example.post_service.service.interfaces.ICommentPostShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommentPostShareServiceImpl implements ICommentPostShareService {

    private final CommentRepository commentRepository;
    private final PostShareRepository postShareRepository;
    private final ProducerNotification producerNotification;
    private final ProducerPostEvent producerEvent;
    private final ICommentMapperService commentMapperService;
    private final ProducerLog logProducer;

    public CommentPostShareServiceImpl(CommentRepository commentRepository,
                                       PostShareRepository postShareRepository,
                                       ProducerNotification producerNotification,
                                       ProducerPostEvent producerEvent,
                                       ICommentMapperService commentMapperService,
                                       ProducerLog logProducer){
        this.commentRepository = commentRepository;
        this.postShareRepository = postShareRepository;
        this.producerNotification = producerNotification;
        this.producerEvent = producerEvent;
        this.commentMapperService = commentMapperService;
        this.logProducer = logProducer;
    }

    @Override
    @Transactional
    public List<CommentResponse> getAllCommentsByShareId(Long shareId, String token) {
        try {
            PostShare postShare = postShareRepository.findById(shareId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            return commentRepository.findByPostShareOrderByCreatedAtDesc(postShare).stream()
                    .map(commentMapperService::mapToCommentResponse)
                    .toList();
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Comment PostShare] Failed to get all comments by post share: " + shareId + ". Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_GET_COMMENT);
        }
    }

    @Override
    @Transactional
    public Comment createPostShareComment(Long shareId, CommentRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Optional<PostShare> postOpt = postShareRepository.findById(shareId);
            if (postOpt.isEmpty()) {
                throw new AppException(NotExistedErrorCode.POST_NOT_EXITS);
            }
            PostShare post = postOpt.get();
            Comment comment = new Comment();
            comment.setPostShare(post);
            comment.setUserId(userId);
            comment.setContent(request.getContent());
            Comment savedComment = commentRepository.save(comment);
            producerEvent.publishCommentEvent(savedComment, PostEventType.COMMENT_ADDED, userId, httpRequest);
            producerNotification.sendNotification("NEW_COMMENT", userId, post.getUserId(), shareId, token);
            return savedComment;
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Comment PostShare] Failed to create comments for post share. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_CREATE_COMMENT);
        }
    }

    @Override
    @Transactional
    public CommentResponse updatePostShareComment(Long commentId, String userId, CommentRequest request, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            if (!comment.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.ACCESS_NOT_EDIT_COMMENT);
            }
            Comment oldCommentCopy = new Comment();
            oldCommentCopy.setCommentId(comment.getCommentId());
            oldCommentCopy.setPostShare(comment.getPostShare());
            oldCommentCopy.setUserId(comment.getUserId());
            oldCommentCopy.setContent(comment.getContent());
            comment.setContent(request.getContent());
            Comment updatedComment = commentRepository.save(comment);
            producerEvent.publishCommentEvent(updatedComment, PostEventType.COMMENT_UPDATED, userId, httpRequest);
            PostShare post = postShareRepository.findById(updatedComment.getPostShare().getShareId())
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            producerNotification.sendNotification("COMMENT_UPDATED", userId, post.getUserId(), updatedComment.getPostShare().getShareId(), token);
            return commentMapperService.mapToCommentResponse(updatedComment);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[Comment PostShare] Failed to update comments. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_UPDATE_COMMENT);
        }
    }

    @Override
    @Transactional
    public void deletePostShareComment(Long commentId, String userId, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            if (!comment.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.ACCESS_NOT_DELETE_COMMENT)   ;
            }
            producerEvent.publishCommentEvent(comment, PostEventType.COMMENT_DELETED, userId, httpRequest);
            PostShare post = postShareRepository.findById(comment.getPostShare().getShareId())
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            producerNotification.sendNotification("COMMENT_DELETED", userId, post.getUserId(), comment.getPost().getPostId(), token);
            commentRepository.delete(comment);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to delete comments for post share. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_DELETE_COMMENT);
        }
    }
}
