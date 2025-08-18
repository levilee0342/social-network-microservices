package com.example.post_service.controller;

import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.dto.response.PostAuditResponse;
import com.example.post_service.enums.PostEventType;
import com.example.post_service.service.AuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditTrailService auditService;

    @GetMapping("/posts/{postId}/history")
    public ResponseEntity<ApiResponse<Page<PostAuditResponse>>> getPostHistory(
            @PathVariable Long postId,
            Pageable pageable) {
        Page<PostAuditResponse> history = auditService.getPostHistory(postId, pageable);
        ApiResponse<Page<PostAuditResponse>> apiResponse = ApiResponse.<Page<PostAuditResponse>>builder()
                .code(200)
                .message("Post history retrieved successfully")
                .result(history)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/users/{userId}/activity")
    public ResponseEntity<ApiResponse<List<PostAuditResponse>>> getUserActivity(@PathVariable String userId) {
        List<PostAuditResponse> activity = auditService.getUserActivity(userId);
        ApiResponse<List<PostAuditResponse>> apiResponse = ApiResponse.<List<PostAuditResponse>>builder()
                .code(200)
                .message("User activity retrieved successfully")
                .result(activity)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/events/{eventType}")
    public ResponseEntity<ApiResponse<List<PostAuditResponse>>> getEventsByType(@PathVariable PostEventType eventType) {
        List<PostAuditResponse> events = auditService.getEventsByType(eventType);
        ApiResponse<List<PostAuditResponse>> apiResponse = ApiResponse.<List<PostAuditResponse>>builder()
                .code(200)
                .message("Events by type retrieved successfully")
                .result(events)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/events/date-range")
    public ResponseEntity<ApiResponse<List<PostAuditResponse>>> getEventsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PostAuditResponse> events = auditService.getEventsInDateRange(startDate, endDate);
        ApiResponse<List<PostAuditResponse>> apiResponse = ApiResponse.<List<PostAuditResponse>>builder()
                .code(200)
                .message("Events in date range retrieved successfully")
                .result(events)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/posts/{postId}/state-at")
    public ResponseEntity<ApiResponse<String>> getPostStateAtTime(
            @PathVariable Long postId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime targetTime) {
        String postState = auditService.getPostStateAtTime(postId, targetTime);
        if (postState != null) {
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(200)
                    .message("Post state retrieved successfully")
                    .result(postState)
                    .build();
            return ResponseEntity.ok(apiResponse);
        } else {
            ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("Post state not found at specified time")
                    .result(null)
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }
    }
}