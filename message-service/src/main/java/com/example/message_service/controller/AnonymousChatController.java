package com.example.message_service.controller;

import com.example.message_service.dto.request.AnonymousChatRequest;
import com.example.message_service.dto.response.ApiResponse;
import com.example.message_service.service.interfaces.IAnonymousChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/anonymous-chat")
public class AnonymousChatController {

    private final IAnonymousChatService anonymousChatService;

    public AnonymousChatController(IAnonymousChatService anonymousChatService) {
        this.anonymousChatService = anonymousChatService;
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestAnonymousChat(@RequestBody AnonymousChatRequest request, HttpServletRequest httprequest) {
        anonymousChatService.requestAnonymousChat(request, httprequest);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(200)
                        .message("Anonymous chat request received successfully")
                        .result(null)
                        .build()
        );
    }
}
