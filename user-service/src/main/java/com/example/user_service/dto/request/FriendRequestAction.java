package com.example.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendRequestAction {
    @NotBlank
    private String requestId;
    @NotBlank
    private String receiverId;
}
