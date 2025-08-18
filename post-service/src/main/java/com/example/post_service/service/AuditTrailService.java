package com.example.post_service.service;

import com.example.post_service.dto.response.PostAuditResponse;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.service.interfaces.IAuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditTrailService {

    private final IAuditTrailService auditTrailService;

    public AuditTrailService(IAuditTrailService auditTrailService) {
        this.auditTrailService = auditTrailService;
    }

    public Page<PostAuditResponse> getPostHistory(Long postId, Pageable pageable) {
        return auditTrailService.getPostHistory(postId, pageable);
    }

    public List<PostAuditResponse> getUserActivity(String userId) {
        return auditTrailService.getUserActivity(userId);
    }

    public List<PostAuditResponse> getEventsByType(PostEventType eventType) {
        return auditTrailService.getEventsByType(eventType);
    }

    public List<PostAuditResponse> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditTrailService.getEventsInDateRange(startDate, endDate);
    }

    public String getPostStateAtTime(Long postId, LocalDateTime targetTime) {
        return auditTrailService.getPostStateAtTime(postId, targetTime);
    }
}