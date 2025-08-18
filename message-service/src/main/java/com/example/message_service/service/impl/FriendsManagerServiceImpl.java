package com.example.message_service.service.impl;

import com.example.message_service.config.ServiceProperties;
import com.example.message_service.dto.response.ApiResponse;
import com.example.message_service.kafka.producer.ProducerLog;
import com.example.message_service.service.interfaces.IFriendsManagerService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class FriendsManagerServiceImpl implements IFriendsManagerService {

    private final WebClient.Builder webClientBuilder;
    private final ProducerLog producerLog;
    private final ServiceProperties serviceProperties;

    public FriendsManagerServiceImpl(WebClient.Builder webClientBuilder,
                                     ProducerLog producerLog,
                                     ServiceProperties serviceProperties) {
        this.webClientBuilder = webClientBuilder;
        this.producerLog = producerLog;
        this.serviceProperties = serviceProperties;
    }

    @Override
    public boolean isFriend(String userId, String postUserId, String token) {
        try {
            WebClient webClient = webClientBuilder.build();
            Mono<ApiResponse<Boolean>> responseMono = webClient.get()
                    .uri(serviceProperties.getFriend().getUrl() + "/{userId}/is-friend/{friendId}", userId, postUserId)
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Boolean>>() {});

            ApiResponse<Boolean> response = responseMono.block();
            return response != null && Boolean.TRUE.equals(response.getResult());

        } catch (Exception e) {
            producerLog.sendLog(
                    "post-service",
                    "ERROR",
                    "[Post] Error checking friendship between userId: " + userId +
                            " and postUserId: " + postUserId + ". Error: " + e.getMessage());
            return false;
        }
    }

}