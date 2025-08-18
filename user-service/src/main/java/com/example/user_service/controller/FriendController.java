package com.example.user_service.controller;

import com.example.user_service.dto.request.FriendRequestAction;
import com.example.user_service.dto.request.SendFriendRequest;
import com.example.user_service.dto.response.ApiResponse;
import com.example.user_service.dto.response.FriendRequestResponse;
import com.example.user_service.dto.response.FriendResponse;
import com.example.user_service.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@Validated
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendRequestResponse>> sendFriendRequest(@Validated @RequestBody SendFriendRequest request, HttpServletRequest httpRequest) {
        FriendRequestResponse response = friendService.sendFriendRequest(request.getSenderId(), request.getReceiverId(), httpRequest);
        return ResponseEntity.ok(
                ApiResponse.<FriendRequestResponse>builder()
                        .code(200)
                        .message("Friend request sent successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/request/accept")
    public ResponseEntity<ApiResponse<FriendRequestResponse>> acceptFriendRequest(@Validated @RequestBody FriendRequestAction request, HttpServletRequest httpRequest) {
        FriendRequestResponse response = friendService.acceptFriendRequest(request.getRequestId(), request.getReceiverId(), httpRequest);
        return ResponseEntity.ok(
                ApiResponse.<FriendRequestResponse>builder()
                        .code(200)
                        .message("Friend request accepted successfully")
                        .result(response)
                        .build()
        );
    }

    @PostMapping("/request/reject")
    public ResponseEntity<ApiResponse<FriendRequestResponse>> rejectFriendRequest(@Validated @RequestBody FriendRequestAction request) {
        FriendRequestResponse response = friendService.rejectFriendRequest(request.getRequestId(), request.getReceiverId());
        return ResponseEntity.ok(
                ApiResponse.<FriendRequestResponse>builder()
                        .code(200)
                        .message("Friend request rejected successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends() {
        String userId = getCurrentUserId();
        List<FriendResponse> response = friendService.getFriends(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<FriendResponse>>builder()
                        .code(200)
                        .message("Friends retrieved successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingFriendRequests() {
        String userId = getCurrentUserId();
        List<FriendRequestResponse> response = friendService.getPendingFriendRequests(userId);
        return ResponseEntity.ok(
                ApiResponse.<List<FriendRequestResponse>>builder()
                        .code(200)
                        .message("Pending friend requests retrieved successfully")
                        .result(response)
                        .build()
        );
    }

    @DeleteMapping("/{userId}/remove/{friendId}")
    public ResponseEntity<ApiResponse<Boolean>> removeFriend(@PathVariable @NotBlank String userId, @PathVariable @NotBlank String friendId) {
        boolean response = friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .code(200)
                        .message("Friend removed successfully")
                        .result(response)
                        .build()
        );
    }

    @GetMapping("/{userId}/is-friend/{friendId}")
    public ResponseEntity<ApiResponse<Boolean>> isFriend(
            @PathVariable @NotBlank String userId,
            @PathVariable @NotBlank String friendId) {

        boolean isFriend = friendService.isFriend(userId, friendId);

        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .code(200)
                        .message(isFriend ? "They are friends" : "They are not friends")
                        .result(isFriend)
                        .build()
        );
    }

}