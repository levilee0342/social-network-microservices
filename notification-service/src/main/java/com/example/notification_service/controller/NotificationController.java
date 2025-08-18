package com.example.notification_service.controller;

import com.example.notification_service.dto.request.UserPreferenceRequest;
import com.example.notification_service.dto.response.ApiResponse;
import com.example.notification_service.dto.response.NotificationResponse;
import com.example.notification_service.dto.response.UserPreferenceResponse;
import com.example.notification_service.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private String getCurrentUserId(HttpServletRequest request) {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean onlyUnread,
            HttpServletRequest request) {
        Page<NotificationResponse> notifications = notificationService.getNotifications(
                getCurrentUserId(request), onlyUnread, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.<Page<NotificationResponse>>builder()
                .code(200)
                .message("Get notifications successfully")
                .result(notifications)
                .build());
    }

    @PutMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@RequestBody List<Long> notificationIds, HttpServletRequest request) {
        notificationService.markAsRead(getCurrentUserId(request), notificationIds);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Marked notifications as read")
                .result(null)
                .build());
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long notificationId, HttpServletRequest request) {
        notificationService.deleteNotification(getCurrentUserId(request), notificationId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Notification deleted successfully")
                .result(null)
                .build());
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updatePreferences(
            @RequestBody UserPreferenceRequest preferenceRequest,
            HttpServletRequest request) {
        UserPreferenceResponse response = notificationService.updatePreferences(getCurrentUserId(request), preferenceRequest);
        return ResponseEntity.ok(ApiResponse.<UserPreferenceResponse>builder()
                .code(200)
                .message("Updated preferences successfully")
                .result(response)
                .build());
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getPreferences(HttpServletRequest request) {
        UserPreferenceResponse response = notificationService.getPreferences(getCurrentUserId(request));
        return ResponseEntity.ok(ApiResponse.<UserPreferenceResponse>builder()
                .code(200)
                .message("Get preferences successfully")
                .result(response)
                .build());
    }
}
