package com.example.message_service.controller;

import com.example.message_service.dto.response.ApiResponse;
import com.example.message_service.entity.ConversationDocument;
import com.example.message_service.entity.MessageDocument;
import com.example.message_service.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/all-messages/{conversationId}")
    public ResponseEntity<ApiResponse<List<MessageDocument>>> getAllMessagesOfAConversation(
            @PathVariable String conversationId, HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        List<MessageDocument> messages = messageService.getAllMessagesOfAConversation(conversationId, token);

        return ResponseEntity.ok(
                ApiResponse.<List<MessageDocument>>builder()
                        .code(200)
                        .message("Fetch all messages of a conversation successfully")
                        .result(messages)
                        .build()
        );
    }


    @PostMapping
    public ResponseEntity<ApiResponse<MessageDocument>> sendMessage(@RequestBody MessageDocument message, HttpServletRequest httpRequest) {
        String userId = SecurityContextHolder.getContext().getAuthentication() != null
                ? (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;

        if (userId == null || !userId.equals(message.getSenderId())) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.<MessageDocument>builder()
                            .code(401)
                            .message("Unauthorized: Invalid sender")
                            .build());
        }
        String token = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        MessageDocument sentMessage = messageService.sendMessage(message, token, userId);
        return ResponseEntity.ok(ApiResponse.<MessageDocument>builder()
                .code(200)
                .message("Message sent successfully")
                .result(sentMessage)
                .build());
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(@PathVariable String messageId,
                                                               @RequestParam String userId) {
        messageService.markMessageAsRead(messageId, userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Message marked as read successfully")
                .build());
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessageForUser(@PathVariable String messageId) {
        String userId = SecurityContextHolder.getContext().getAuthentication() != null
                ? (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        messageService.deleteMessageForUser(messageId,userId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(200)
                .message("Message deleted successfully for user")
                .build());
    }
}
