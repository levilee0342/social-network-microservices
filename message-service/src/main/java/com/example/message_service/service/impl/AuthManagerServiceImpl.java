package com.example.message_service.service.impl;

import com.example.message_service.dto.response.ApiResponse;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.service.interfaces.IAuthManagerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class AuthManagerServiceImpl implements IAuthManagerService {
    private final WebClient.Builder webClientBuilder;
    private final ProducerLog producerLog;

    public AuthManagerServiceImpl(WebClient.Builder webClientBuilder, ProducerLog producerLog) {
        this.producerLog = producerLog;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public boolean checkExitsUser(HttpServletRequest request) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            WebClient webClient = webClientBuilder.build();
            Mono<ApiResponse<Boolean>> responseMono = webClient.get()
                    .uri("http://identity-service/identity/check-user?userId=" + userId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Boolean>>() {});
            ApiResponse<Boolean> response = responseMono.block();
            System.out.println("Dữ liệu: " + response);
            return response != null && Boolean.TRUE.equals(response.getResult());
        } catch (Exception e) {
            producerLog.sendLog(
                    "message-service",
                    "ERROR",
                    "[AuthManagerService] Lỗi khi kiểm tra tồn tại userId: " + userId + ". Chi tiết: " + e.getMessage());
            return false;
        }
    }
}
