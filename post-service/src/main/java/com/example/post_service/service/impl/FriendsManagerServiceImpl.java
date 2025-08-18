package com.example.post_service.service.impl;

import com.example.post_service.config.ServiceProperties;
import com.example.post_service.dto.response.ApiResponse;
import com.example.post_service.dto.response.FriendResponse;
import com.example.post_service.kafka.producer.ProducerLog;
import com.example.post_service.service.interfaces.IFriendsManagerService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendsManagerServiceImpl implements IFriendsManagerService {
    private final WebClient.Builder webClientBuilder;
    private final ProducerLog logProducer;
    private final ServiceProperties serviceProperties;

    public FriendsManagerServiceImpl(WebClient.Builder webClientBuilder,
                                     ProducerLog logProducer,
                                     ServiceProperties serviceProperties) {
        this.webClientBuilder = webClientBuilder;
        this.logProducer = logProducer;
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
            logProducer.sendLog("post-service", "ERROR",
                    "[Post] Error checking friendship between userId: " + userId +
                            " and postUserId: " + postUserId + ". Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> getFriendIds(String userId, String token) {
        try {
            WebClient webClient = webClientBuilder.build();
            Mono<ApiResponse<List<FriendResponse>>> responseMono = webClient.get()
                    .uri(serviceProperties.getFriend().getUrl())
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<FriendResponse>>>() {});
            ApiResponse<List<FriendResponse>> response = responseMono.block();
            if (response == null || response.getResult() == null) {
                return Collections.emptyList();
            }
            return response.getResult().stream()
                    .map(FriendResponse::getFriendId)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}