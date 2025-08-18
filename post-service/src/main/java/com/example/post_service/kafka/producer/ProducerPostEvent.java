package com.example.post_service.kafka.producer;

import com.example.post_service.entity.Comment;
import com.example.post_service.entity.Post;
import com.example.post_service.entity.PostEvent;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.repository.PostEventRepository;
import com.example.post_service.service.interfaces.IHttpContextHelperService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProducerPostEvent{

    private final PostEventRepository postEventRepository;
    private final ProducerEvent producerEvent;
    private final IHttpContextHelperService httpContextHelper;
    private final ObjectMapper objectMapper;
    private final ProducerLog producerLog;
    private static final Logger logger = LogManager.getLogger(ProducerPostEvent.class);

    public ProducerPostEvent(PostEventRepository postEventRepository,
                             ProducerEvent producerEvent,
                             IHttpContextHelperService httpContextHelper,
                             ObjectMapper objectMapper,
                             ProducerLog producerLog) {
        this.postEventRepository = postEventRepository;
        this.producerEvent = producerEvent;
        this.httpContextHelper = httpContextHelper;
        this.objectMapper = objectMapper;
        this.producerLog = producerLog;
    }

    public void publishPostCreatedEvent(Post post, String userId, HttpServletRequest request) {
        try {
            PostEvent event = createBaseEvent(post.getPostId(), userId, PostEventType.POST_CREATED, request);
            event.setEventData(objectMapper.writeValueAsString(post));
            event.setOccurredAt(LocalDateTime.now());
            PostEvent savedEvent = postEventRepository.save(event);
            producerEvent.publishToKafka(savedEvent);
            logger.info("[Producer Post Event] Sent event to kafka to create post: {}", event);
            producerLog.sendLog(
                    "post_service",
                    "DEBUG",
                    "[Producer Post Event] Sent event to kafka to create post: " + event
            );
        } catch (Exception e) {
            logger.error("[Producer Post Event] Failed sent event to kafka to create post: {}", e.getMessage());
            producerLog.sendLog(
                    "post_service",
                    "ERROR",
                    "[Producer Post Event] Failed sent event to kafka to create post: " + e
            );
        }
    }

    public void publishPostUpdatedEvent(Post newPost, Post oldPost, String userId, HttpServletRequest request) {
        try {
            PostEvent event = createBaseEvent(newPost.getPostId(), userId, PostEventType.POST_UPDATED, request);
            event.setOccurredAt(LocalDateTime.now());
            event.setEventData(objectMapper.writeValueAsString(newPost));
            event.setPreviousData(objectMapper.writeValueAsString(oldPost));
            PostEvent savedEvent = postEventRepository.save(event);
            producerEvent.publishToKafka(savedEvent);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post_service",
                    "ERROR",
                    "[Publisher Event] Error publishing POST_UPDATED event. Reason: " + e
            );
        }
    }

    public void publishPostDeletedEvent(Post post, String userId, HttpServletRequest request) {
        try {
            PostEvent event = createBaseEvent(post.getPostId(), userId, PostEventType.POST_DELETED, request);
            event.setPreviousData(objectMapper.writeValueAsString(post));
            event.setOccurredAt(LocalDateTime.now());
            event.setReason("User deleted post");
            PostEvent savedEvent = postEventRepository.save(event);
            producerEvent.publishToKafka(savedEvent);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post_service",
                    "ERROR",
                    "[Publisher Event] Error publishing POST_DELETED event. Reason: " + e
            );
        }
    }

    public void publishCommentEvent(Comment comment, PostEventType eventType, String userId, HttpServletRequest request) {
        try {
            PostEvent event = createBaseEvent(comment.getPost().getPostId(), userId, eventType, request);
            event.setEventData(objectMapper.writeValueAsString(comment));
            event.setOccurredAt(LocalDateTime.now());
            PostEvent savedEvent = postEventRepository.save(event);
            producerEvent.publishToKafka(savedEvent);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post_service",
                    "ERROR",
                    "[Publisher Event] Error publishing comment event. Reason: " + e
            );
        }
    }

    public void publishLikeEvent(Long postId, String userId, PostEventType eventType, HttpServletRequest request) {
        try {
            PostEvent event = createBaseEvent(postId, userId, eventType, request);
            event.setOccurredAt(LocalDateTime.now());
            event.setEventData("{\"action\":\"" + eventType.name() + "\"}");
            PostEvent savedEvent = postEventRepository.save(event);
            producerEvent.publishToKafka(savedEvent);
        } catch (Exception e) {
            producerLog.sendLog(
                    "post_service",
                    "ERROR",
                    "[Publisher Event] Error publishing reaction event. Reason: " + e
            );
        }
    }

    private PostEvent createBaseEvent(Long postId, String userId, PostEventType eventType, HttpServletRequest request) {
        PostEvent event = new PostEvent();
        event.setPostId(postId);
        event.setUserId(userId);
        event.setEventType(eventType);
        if (request != null) {
            event.setIpAddress(httpContextHelper.getClientIpAddress(request));
            event.setUserAgent(httpContextHelper.getUserAgent(request));
        }
        return event;
    }
}