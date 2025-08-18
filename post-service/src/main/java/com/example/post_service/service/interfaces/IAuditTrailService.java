package com.example.post_service.service.interfaces;

import com.example.post_service.dto.response.PostAuditResponse;
import com.example.post_service.enums.PostEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IAuditTrailService {
    Page<PostAuditResponse> getPostHistory(Long postId, Pageable pageable);
    List<PostAuditResponse> getUserActivity(String userId);
    List<PostAuditResponse> getEventsByType(PostEventType eventType);
    List<PostAuditResponse> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate);
    String getPostStateAtTime(Long postId, LocalDateTime targetTime);
}