package com.example.user_service.controller;

import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.service.BlockUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/block")
public class BlockController {

    private final BlockUserService blockUserService;

    public BlockController(BlockUserService blockUserService) {
        this.blockUserService = blockUserService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> block(@RequestParam String blockerId, @RequestParam String blockedId) {
        blockUserService.blockUser(blockerId, blockedId);
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
        blockUserService.unblockUser(blockerId, blockedId);
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
        boolean result = blockUserService.isBlocked(user1, user2);
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .code(200)
                        .message("Check block status successful")
                        .result(result)
                        .build()
        );
    }
}

