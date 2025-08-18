package com.example.message_service.controller;

import com.example.message_service.dto.response.ApiResponse;
import com.example.message_service.entity.ConversationDocument;
import com.example.message_service.service.ConversationService;
import com.example.message_service.service.interfaces.IConversationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationDocument>>> getAllConversationsOfUser(HttpServletRequest request) {
        List<ConversationDocument> conversations = conversationService.getAllConversationsOfUser(request);
        return ResponseEntity.ok(ApiResponse.<List<ConversationDocument>>builder()
                .code(200)
                .message("Get all conversations of user successfully")
                .result(conversations)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationDocument>> createConversation(@RequestBody ConversationDocument conversation, HttpServletRequest request) {
        ConversationDocument createdConversation = conversationService.createConversation(conversation, request);
        return ResponseEntity.ok(ApiResponse.<ConversationDocument>builder()
                .code(200)
                .message("Conversation created successfully")
                .result(createdConversation)
                .build());
    }

}
