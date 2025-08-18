package com.example.post_service.service.impl;

import com.example.post_service.dto.response.PostAuditResponse;
import com.example.post_service.entity.PostEvent;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.error.AuthErrorCode;
import com.example.post_service.exception.AppException;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.repository.PostEventRepository;
import com.example.post_service.service.interfaces.IAuditTrailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditTrailServiceImpl implements IAuditTrailService {

    private final PostEventRepository postEventRepository;
    private final ProducerLog logProducer;
    
    public AuditTrailServiceImpl(PostEventRepository postEventRepository,
                                 ProducerLog logProducer) {
        this.postEventRepository = postEventRepository;
        this.logProducer = logProducer;
    }

    @Override
    public Page<PostAuditResponse> getPostHistory(Long postId, Pageable pageable) {
        try {
            Page<PostEvent> events = postEventRepository.findByPostIdOrderByOccurredAtDesc(postId, pageable);
            return events.map(this::mapToAuditResponse);
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[AuditTrail] Error getting post history for postId: " + postId + ". Error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_POST_HISTORY);
        }
    }

    @Override
    public List<PostAuditResponse> getUserActivity(String userId) {
        try {
            List<PostEvent> events = postEventRepository.findByUserIdOrderByOccurredAtDesc(userId);
            return events.stream()
                    .map(this::mapToAuditResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[AuditTrail] Error getting user activity for userId: " + userId + ". Error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_USER_ACTIVITY);
        }
    }

    @Override
    public List<PostAuditResponse> getEventsByType(PostEventType eventType) {
        try {
            List<PostEvent> events = postEventRepository.findByEventTypeOrderByOccurredAtDesc(eventType);
            return events.stream()
                    .map(this::mapToAuditResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[AuditTrail] Error getting events by type: " + eventType + ". Error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_EVENT);
        }
    }

    @Override
    public List<PostAuditResponse> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            List<PostEvent> events = postEventRepository.findEventsBetweenDates(startDate, endDate);
            return events.stream()
                    .map(this::mapToAuditResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[AuditTrail] Error getting events from " + startDate + " to " + endDate + ". Error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_EVENT_IN_DAY);
        }
    }

    @Override
    public String getPostStateAtTime(Long postId, LocalDateTime targetTime) {
        try {
            List<PostEvent> events = postEventRepository.findByPostIdOrderByOccurredAtDesc(postId);
            for (PostEvent event : events) {
                if (event.getOccurredAt().isBefore(targetTime) || event.getOccurredAt().isEqual(targetTime)) {
                    if (event.getEventType() == PostEventType.POST_CREATED ||
                            event.getEventType() == PostEventType.POST_UPDATED) {
                        return event.getEventData();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logProducer.sendLog(
                    "post-service",
                    "ERROR",
                    "[AuditTrail] Error retrieving post state at time " + targetTime +
                            " for postId: " + postId + ". Error: " + e.getMessage());
            throw new AppException(AuthErrorCode.FAILED_GET_POST_STATE);
        }
    }

    private PostAuditResponse mapToAuditResponse(PostEvent event) {
        PostAuditResponse response = new PostAuditResponse();
        response.setEventId(event.getEventId());
        response.setPostId(event.getPostId());
        response.setUserId(event.getUserId());
        response.setEventType(event.getEventType().name());
        response.setEventData(event.getEventData());
        response.setPreviousData(event.getPreviousData());
        response.setIpAddress(event.getIpAddress());
        response.setUserAgent(event.getUserAgent());
        response.setReason(event.getReason());
        response.setOccurredAt(event.getOccurredAt());
        return response;
    }
}