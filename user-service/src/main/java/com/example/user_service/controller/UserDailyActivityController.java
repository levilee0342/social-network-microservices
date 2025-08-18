package com.example.user_service.controller;

import com.example.user_service.service.impl.UserDailyActivityServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/activity")
@RequiredArgsConstructor
public class UserDailyActivityController {

    private final UserDailyActivityServiceImpl userDailyActivityService;

    @PostMapping("/record")
    public ResponseEntity<Void> recordActivity(@RequestParam String userId) {
        userDailyActivityService.recordActivity(userId);
        return ResponseEntity.ok().build();
    }
}
