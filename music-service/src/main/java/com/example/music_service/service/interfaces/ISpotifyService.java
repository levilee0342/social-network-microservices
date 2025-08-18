package com.example.music_service.service.interfaces;

import com.example.music_service.dto.response.SpotifyTokenResponse;
import reactor.core.publisher.Mono;

public interface ISpotifyService {
    String buildSpotifyLoginUrl(String userId);
    Mono<SpotifyTokenResponse> exchangeCodeForToken(String code);
    Mono<String> getUserProfile(String accessToken);
    String getAccessTokenForUser(String userId);
    SpotifyTokenResponse refreshToken(String refreshToken);
}
