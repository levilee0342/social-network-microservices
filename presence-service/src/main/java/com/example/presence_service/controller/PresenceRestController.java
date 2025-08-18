package com.example.presence_service.controller;

import com.example.presence_service.entity.UserPresence;
import com.example.presence_service.service.interfaces.IPresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/presence")
public class PresenceRestController {
    private final IPresenceService presenceService;

    public PresenceRestController(IPresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserPresence> getUserPresence(@PathVariable String userId) {
        try {
            UserPresence presence = presenceService.getPresence(userId);
            if (presence == null) {
                System.out.println("No presence found for userId: " + userId);
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(presence);
        } catch (Exception e) {
            System.out.println("Failed to get presence for userId: " + userId + ", error: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}