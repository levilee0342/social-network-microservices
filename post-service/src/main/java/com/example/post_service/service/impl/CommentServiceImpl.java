package com.example.post_service.service.impl;

import com.example.post_service.dto.request.CommentRequest;
import com.example.post_service.dto.request.UserInteractionEventRequest;
import com.example.post_service.dto.response.CommentResponse;
import com.example.post_service.entity.Comment;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.TypeContent;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerPostEvent;
import com.example.post_service.kafka.producer.ProducerUserPreference;
import com.example.post_service.repository.CommentRepository;
import com.example.post_service.repository.PostRepository;
import com.example.post_service.service.interfaces.ICommentMapperService;
import com.example.post_service.service.interfaces.ICommentService;
import com.example.post_service.enums.PostEventType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ProducerNotification producerNotification;
    private final ProducerPostEvent producerEvent;
    private final ICommentMapperService commentMapperService;
    private final ProducerUserPreference producerUserPreference;
    private final ProducerLog logProducer;

    public CommentServiceImpl(CommentRepository commentRepository,
                              PostRepository postRepository,
                              ProducerNotification producerNotification,
                              ProducerPostEvent producerEvent,
                              ICommentMapperService commentMapperService,
                              ProducerUserPreference producerUserPreference,
                              ProducerLog logProducer){
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.producerNotification = producerNotification;
        this.producerEvent = producerEvent;
        this.commentMapperService = commentMapperService;
        this.producerUserPreference = producerUserPreference;
        this.logProducer = logProducer;
    }

    @Override
    @Transactional
    public List<CommentResponse> getAllCommentsByPostId(Long postId, String token) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            return commentRepository.findByPostOrderByCreatedAtDesc(post).stream()
                    .map(commentMapperService::mapToCommentResponse)
                    .toList();
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to get all comments by postId: " + postId + ". Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_GET_COMMENT);
        }
    }

    @Override
    @Transactional
    public Comment createComment(Long postId, CommentRequest request, String userId, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                throw new AppException(NotExistedErrorCode.POST_NOT_EXITS);
            }
            Post post = postOpt.get();
            Comment comment = new Comment();
            comment.setPost(post);
            comment.setUserId(userId);
            comment.setContent(request.getContent());
            Comment savedComment = commentRepository.save(comment);
            producerEvent.publishCommentEvent(savedComment, PostEventType.COMMENT_ADDED, userId, httpRequest);
            // Pub sự kiện xử lý điểm tương tác
            ArrayList<Long> typeIds = post.getTypeContent().stream()
                    .map(TypeContent::getTypeId)
                    .collect(Collectors.toCollection(ArrayList::new));

            UserInteractionEventRequest reactionRequest = new UserInteractionEventRequest();
            reactionRequest.setUserId(userId);
            reactionRequest.setEventType("COMMENT");
            reactionRequest.setTypeIds(typeIds);
            reactionRequest.setTimestamp(System.currentTimeMillis());

            producerUserPreference.publisherUserPreferenceEvent(reactionRequest);
            System.out.println("[DEBUG] Sending user interaction event: " + reactionRequest);
            producerNotification.sendNotification("NEW_COMMENT", userId, post.getUserId(), postId, token);
            return savedComment;
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to create comments. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_CREATE_COMMENT);
        }
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, String userId, CommentRequest request, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            if (!comment.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.ACCESS_NOT_EDIT_COMMENT);
            }
            Comment oldCommentCopy = new Comment();
            oldCommentCopy.setCommentId(comment.getCommentId());
            oldCommentCopy.setPost(comment.getPost());
            oldCommentCopy.setUserId(comment.getUserId());
            oldCommentCopy.setContent(comment.getContent());
            comment.setContent(request.getContent());
            Comment updatedComment = commentRepository.save(comment);
            producerEvent.publishCommentEvent(updatedComment, PostEventType.COMMENT_UPDATED, userId, httpRequest);
            Post post = postRepository.findById(updatedComment.getPost().getPostId())
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            producerNotification.sendNotification("COMMENT_UPDATED", userId, post.getUserId(), updatedComment.getPost().getPostId(), token);
            return commentMapperService.mapToCommentResponse(updatedComment);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to update comments. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_UPDATE_COMMENT);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String userId, HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            if (!comment.getUserId().equals(userId)) {
                throw new AppException(AuthErrorCode.ACCESS_NOT_DELETE_COMMENT)   ;
            }
            producerEvent.publishCommentEvent(comment, PostEventType.COMMENT_DELETED, userId, httpRequest);
            Post post = postRepository.findById(comment.getPost().getPostId())
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            producerNotification.sendNotification("COMMENT_DELETED", userId, post.getUserId(), comment.getPost().getPostId(), token);
            commentRepository.delete(comment);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "Failed to delete comments. Error: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_DELETE_COMMENT);
        }
    }
}