package com.example.message_service.controller;

import com.example.message_service.dto.response.ApiResponse;
import com.example.message_service.repository.ICouchbaseBlockMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/block-message")
public class BlockController {

    private final ICouchbaseBlockMessageRepository ICouchbaseBlockMessageRepository;

    public BlockController(ICouchbaseBlockMessageRepository ICouchbaseBlockMessageRepository) {
        this.ICouchbaseBlockMessageRepository = ICouchbaseBlockMessageRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> block(@RequestParam String blockerId, @RequestParam String blockedId) {
        ICouchbaseBlockMessageRepository.blockMessage(blockerId, blockedId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Blocked successfully")
                        .result(null)
                        .build()
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unblock(@RequestParam String blockerId, @RequestParam String blockedId) {
        ICouchbaseBlockMessageRepository.unblockMessage(blockerId, blockedId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Unblocked successfully")
                        .result(null)
                        .build()
        );
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> isBlocked(@RequestParam String user1, @RequestParam String user2) {
        boolean result = ICouchbaseBlockMessageRepository.isBlocked(user1, user2);
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .code(200)
                        .message("Check block status successful")
                        .result(result)
                        .build()
        );
    }
}

