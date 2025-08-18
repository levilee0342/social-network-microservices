package com.example.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendFriendRequest {
    @NotBlank
    private String senderId;
    @NotBlank
    private String receiverId;
}