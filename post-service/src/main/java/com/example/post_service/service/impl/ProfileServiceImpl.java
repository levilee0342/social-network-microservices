package com.example.post_service.service.impl;

import com.example.post_service.config.ServiceProperties;
import com.example.post_service.dto.response.GraphQLResponse;
import com.example.post_service.dto.response.ProfileResponse;
import com.example.post_service.service.interfaces.IProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ProfileServiceImpl implements IProfileService {

    private final WebClient.Builder webClientBuilder;
    private final ServiceProperties serviceProperties;
    private final ObjectMapper objectMapper;

    public ProfileServiceImpl(WebClient.Builder webClientBuilder,
                              ServiceProperties serviceProperties,
                              ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.serviceProperties = serviceProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public ProfileResponse getProfile(String token) {
        String graphqlQuery = """
        query {
            getProfile {
                profileId
                userId
                fullName
                dateOfBirth
                address
                phone
                avatarUrl
                isPublic
            }
        }
        """;
        Map<String, Object> requestBody = Map.of("query", graphqlQuery);
        try {
            WebClient webClient = webClientBuilder.build();
            GraphQLResponse response = webClient.post()
                    .uri(serviceProperties.getProfile().getUrl())
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GraphQLResponse.class)
                    .block();
            Map<String, Object> data = response.getData();
            Object rawProfile = data.get("getProfile");
            return objectMapper.convertValue(rawProfile, ProfileResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ProfileResponse getPublicProfileByUserId(String userId) {
        String graphqlQuery = """
        query($userId: String!) {
            getProfileByUserId(userId: $userId) {
                userId
                fullName
                avatarUrl
            }
        }
    """;

        Map<String, Object> variables = Map.of("userId", userId);
        Map<String, Object> requestBody = Map.of(
                "query", graphqlQuery,
                "variables", variables
        );

        try {
            WebClient webClient = webClientBuilder.build();
            GraphQLResponse response = webClient.post()
                    .uri(serviceProperties.getProfile().getUrl())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GraphQLResponse.class)
                    .block();

            Map<String, Object> data = response.getData();
            Object rawProfile = data.get("getProfileByUserId");
            return objectMapper.convertValue(rawProfile, ProfileResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

}
