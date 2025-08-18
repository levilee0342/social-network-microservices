package com.example.music_service.service.impl;

import com.example.music_service.dto.response.SpotifyTokenResponse;
import com.example.music_service.entity.User;
import com.example.music_service.repository.UserRepository;
import com.example.music_service.service.interfaces.ISpotifyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static com.example.music_service.constant.SpotifyConstants.SCOPES;

@Service
public class SpotifyServiceImpl implements ISpotifyService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spotify.auth-uri}")
    private String authUri;

    @Value("${spotify.token-uri}")
    private String tokenUri;

    @Value("${spotify.user-uri}")
    private String userUri;

    private final WebClient webClient = WebClient.builder().build();
    private final UserRepository userRepository;

    public SpotifyServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String buildSpotifyLoginUrl(String userId) {
        String state = Base64.getUrlEncoder().encodeToString(userId.getBytes(StandardCharsets.UTF_8));

        return UriComponentsBuilder.fromHttpUrl(authUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("scope", SCOPES)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .encode()
                .build()
                .toUriString();
    }

    public Mono<SpotifyTokenResponse> exchangeCodeForToken(String code) {
        String credentials = clientId + ":" + clientSecret;
        String basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("code", code)
                        .with("redirect_uri", redirectUri))
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    System.err.println("Spotify token error: " + body);
                                    return Mono.error(new RuntimeException("Spotify token exchange failed: " + body));
                                }))
                .bodyToMono(SpotifyTokenResponse.class);
    }

    public Mono<String> getUserProfile(String accessToken) {
        return webClient.get()
                .uri(userUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    System.err.println("Spotify profile error: " + body);
                                    return Mono.error(new RuntimeException("Spotify profile fetch failed: " + body));
                                }))
                .bodyToMono(String.class);
    }

    public void linkSpotifyToUser(String userId, String spotifyUserId, SpotifyTokenResponse tokenResponse) {
        User user = userRepository.findById(userId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUserId(userId);
                    return newUser;
                });

        user.setSpotifyUserId(spotifyUserId);
        user.setAccessToken(tokenResponse.getAccessToken());
        user.setRefreshToken(tokenResponse.getRefreshToken());
        user.setTokenExpirationTime(Instant.now().plusSeconds(tokenResponse.getExpiresIn()).toEpochMilli());
        user.setScope(tokenResponse.getScope());

        userRepository.save(user);
    }

    public String getAccessTokenForUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Spotify token not found for user: " + userId));

        if (Instant.now().toEpochMilli() > user.getTokenExpirationTime()) {
            // Token hết hạn, refresh tại đây
            SpotifyTokenResponse newToken = refreshToken(user.getRefreshToken());
            user.setAccessToken(newToken.getAccessToken());
            user.setTokenExpirationTime(Instant.now().plusSeconds(newToken.getExpiresIn()).toEpochMilli());
            userRepository.save(user);
            return newToken.getAccessToken();
        }

        return user.getAccessToken();
    }

    public SpotifyTokenResponse refreshToken(String refreshToken) {
        String credentials = clientId + ":" + clientSecret;
        String basicAuthHeader = "Basic " + Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class)
                .block();
    }

}
