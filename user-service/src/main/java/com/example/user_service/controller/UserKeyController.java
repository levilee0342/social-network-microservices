package com.example.user_service.controller;

import com.example.user_service.service.security.UserKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/keys")
@RequiredArgsConstructor
public class UserKeyController {

    private final UserKeyService userKeyService;

    @GetMapping("/public/{userId}")
    public ResponseEntity<String> getPublicKey(@PathVariable String userId) {
        return ResponseEntity.ok(userKeyService.getPublicKeyBase64(userId));
    }

    @PostMapping("/private")
    public ResponseEntity<String> getPrivateKey(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userKeyService.getPrivateKeyBase64FromToken(token));
    }
}
