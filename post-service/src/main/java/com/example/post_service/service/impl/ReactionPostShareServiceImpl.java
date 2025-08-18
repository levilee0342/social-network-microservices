package com.example.post_service.service.impl;

import com.example.post_service.entity.Like;
import com.example.post_service.entity.PostShare;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.enums.ReactionType;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.error.InvalidErrorCode;
import com.example.post_service.error.NotExistedErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.kafka.producer.ProducerNotification;
import com.example.post_service.kafka.producer.ProducerPostEvent;
import com.example.post_service.repository.PostShareRepository;
import com.example.post_service.repository.ReactionRepository;
import com.example.post_service.service.interfaces.IReactionPostShareService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReactionPostShareServiceImpl implements IReactionPostShareService {

    private final ReactionRepository reactionRepository;
    private final PostShareRepository postShareRepository;
    private final ProducerNotification producerNotification;
    private final ProducerPostEvent producerEvent;
    private final ProducerLog producerLog;

    public ReactionPostShareServiceImpl(ReactionRepository reactionRepository,
                                        PostShareRepository postShareRepository,
                                        ProducerNotification producerNotification,
                                        ProducerPostEvent producerEvent,
                                        ProducerLog producerLog) {
        this.reactionRepository = reactionRepository;
        this.postShareRepository = postShareRepository;
        this.producerNotification = producerNotification;
        this.producerEvent = producerEvent;
        this.producerLog = producerLog;
    }

    @Override
    @Transactional
    public void reactToPostShare(Long shareId, ReactionType type, String userId, HttpServletRequest request) {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            PostShare post = postShareRepository.findById(shareId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            Optional<Like> existingReaction = reactionRepository.findByPostShare_ShareIdAndUserId(shareId, userId);
            if (existingReaction.isPresent()) {
                Like existing = existingReaction.get();
                if (existing.getType() == type) {
                    throw new AppException(InvalidErrorCode.YOU_LIKED_POST);
                }
                existing.setType(type);
                reactionRepository.save(existing);
            } else {
                Like reaction = new Like();
                reaction.setPostShare(post);
                reaction.setUserId(userId);
                reaction.setType(type);
                reactionRepository.save(reaction);
            }
            producerEvent.publishLikeEvent(shareId, userId, PostEventType.POST_LIKED, request);
            producerNotification.sendNotification("NEW_LIKE", userId, post.getUserId(), shareId, token);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reaction PostShare] Failed to reaction to post share. Reason: " + e
            );
        }
    }

    @Override
    @Transactional
    public void unreactToPostShare(Long shareId, ReactionType type, String userId, HttpServletRequest request){
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            PostShare post = postShareRepository.findById(shareId)
                    .orElseThrow(() -> new AppException(NotExistedErrorCode.POST_NOT_EXITS));
            Optional<Like> existingReaction = reactionRepository.findByPostShare_ShareIdAndUserId(shareId, userId);
            if (existingReaction.isPresent()) {
                Like existing = existingReaction.get();
                reactionRepository.delete(existing);
            }
            producerEvent.publishLikeEvent(shareId, userId, PostEventType.POST_UNLIKED, request);
            producerNotification.sendNotification("UN_LIKE", userId, post.getUserId(), shareId, token);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Reaction PostShare] Failed to delete reaction to post share. Reason: " + e
            );
        }
    }

    @Override
    public long countReactionsByShareId(Long postId) {
        return reactionRepository.countByPostShare_ShareId(postId);
    }

    @Override
    public Map<String, Integer> countReactionsPostShareGroupByType(Long shareId) {
        try {
            List<Object[]> result = reactionRepository.countReactionsPostShareGroupByType(shareId);
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
                    "[Reaction PostShare] Failed to count reaction for post share. Reason: " + e
            );
            throw new AppException(AuthErrorCode.FAILED_COUNT_REACTION);
        }
    }
}
