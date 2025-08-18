package com.example.post_service.controller;

import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.enums.ReactionType;
import com.example.post_service.service.ReactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/posts/reaction")
public class ReactionController {

    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Reaction post
    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> reactToPost(@PathVariable Long postId, @RequestParam ReactionType type, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        reactionService.reactToPost(postId, type, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Reaction to post successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> unReactToPost(@PathVariable Long postId, @RequestParam ReactionType type, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        reactionService.unreactToPost(postId, type, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Reaction removed successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReactionDetails(
            @PathVariable Long postId
    ) {
        Map<String, Object> result = reactionService.getReactionDetails(postId);

        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.<Map<String, Object>>builder()
                .code(200)
                .message("Reaction details retrieved successfully")
                .result(result)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // Reaction comment
    @PostMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> reactToComment(@PathVariable Long commentId, @RequestParam ReactionType type, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        reactionService.reactToComment(commentId, type, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Reaction to comment successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> unReactToComment(@PathVariable Long commentId, @RequestParam ReactionType type, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        reactionService.unreactToComment(commentId, type, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Reaction removed successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getReactionGroupByCommentId(@PathVariable Long commentId) {
        reactionService.countReactionsCommentGroupByType(commentId);
        ApiResponse<Map<String, Integer>> apiResponse = ApiResponse.<Map<String, Integer>>builder()
                .code(200)
                .message("Reaction comment retrieved successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    //Reaction Post Share
    @PostMapping("/shares/{shareId}")
    public ResponseEntity<ApiResponse<Void>> reactToPostShare(@PathVariable Long shareId, @RequestParam ReactionType type, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        reactionService.reactToPostShare(shareId, type, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Reaction to post share share successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/shares/{shareId}")
    public ResponseEntity<ApiResponse<Void>> unReactToPostShare(@PathVariable Long shareId, @RequestParam ReactionType type, HttpServletRequest httpRequest) {
        String userId = getCurrentUserId();
        reactionService.unreactToPostShare(shareId, type, userId, httpRequest);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(200)
                .message("Reaction of post share removed successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/shares/{shareId}")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getReactionGroupByShareId(@PathVariable Long shareId) {
        reactionService.countReactionsPostShareGroupByType(shareId);
        ApiResponse<Map<String, Integer>> apiResponse = ApiResponse.<Map<String, Integer>>builder()
                .code(200)
                .message("Reaction of post share retrieved successfully")
                .result(null)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
