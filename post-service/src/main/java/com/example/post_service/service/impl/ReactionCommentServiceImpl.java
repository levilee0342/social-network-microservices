package com.example.post_service.service.impl;

import com.example.post_service.entity.Comment;
import com.example.post_service.entity.Like;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.enums.ReactionType;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.error.InvalidErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerPostEvent;
import com.example.post_service.repository.CommentRepository;
import com.example.post_service.repository.ReactionRepository;
import com.example.post_service.service.interfaces.IReactionCommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReactionCommentServiceImpl implements IReactionCommentService {

    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final ProducerNotification producerNotification;
    private final ProducerPostEvent producerEvent;
    private final ProducerLog producerLog;

    public ReactionCommentServiceImpl(ReactionRepository reactionRepository,
                                      CommentRepository commentRepository,
                                      ProducerNotification producerNotification,
                                      ProducerPostEvent producerEvent,
                                      ProducerLog producerLog) {
        this.reactionRepository = reactionRepository;
        this.commentRepository = commentRepository;
        this.producerNotification = producerNotification;
        this.producerEvent = producerEvent;
        this.producerLog = producerLog;
    }

    @Override
    public void reactToComment(Long commentId, ReactionType type, String userId, HttpServletRequest request) {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            Optional<Like> existingReaction = reactionRepository.findByComment_CommentIdAndUserId(commentId, userId);
            if (existingReaction.isPresent()) {
                Like existing = existingReaction.get();
                if (existing.getType() == type) {
                    throw new AppException(InvalidErrorCode.YOU_LIKED_COMMENT);
                }
                existing.setType(type);
                reactionRepository.save(existing);
            } else {
                Like reaction = new Like();
                reaction.setComment(comment);
                reaction.setUserId(userId);
                reaction.setType(type);
                reactionRepository.save(reaction);
            }
            producerEvent.publishLikeEvent(commentId, userId, PostEventType.LIKE_COMMENT, request);
            producerNotification.sendNotification("NEW_LIKE_COMMENT", userId, comment.getUserId(), commentId, token);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reaction Comment] Failed to reaction to comment. Reason: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_REACTION_COMMENT);
        }
    }

    @Override
    public void unreactToComment(Long commentId, ReactionType type, String userId, HttpServletRequest request) {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.COMMENT_NOT_EXITS));
            Optional<Like> existingReaction = reactionRepository.findByComment_CommentIdAndUserId(commentId, userId);
            if (existingReaction.isPresent()) {
                Like existing = existingReaction.get();
                reactionRepository.delete(existing);
            }
            producerEvent.publishLikeEvent(commentId, userId, PostEventType.UNLIKE_COMMENT, request);
            producerNotification.sendNotification("UN_LIKE_COMMENT", userId, comment.getUserId(), commentId, token);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reaction Comment] Failed to delete reaction to comment. Reason: " + e
            );
        }
    }

    @Override
    public long countReactionsCommentByPostId(Long commentId) {
        return reactionRepository.countByComment_CommentId(commentId);
    }

    @Override
    public Map<String, Integer> countReactionsCommentGroupByType(Long commentId) {
        try {
            List<Object[]> result = reactionRepository.countReactionsCommentGroupByType(commentId);
            Map<String, Integer> reactions = new HashMap<>();
            for (ReactionType type : EnumSet.allOf(ReactionType.class)) {
                reactions.put(type.name(), 0);
            }
            for (Object[] row : result) {
                String type = row[0].toString();
                Long count = (Long) row[1];
                reactions.put(type, count.intValue());
            }
            return reactions;
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reaction Comment] Failed to count reaction for comment. Reason: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_COUNT_REACTION);
        }
    }
}
