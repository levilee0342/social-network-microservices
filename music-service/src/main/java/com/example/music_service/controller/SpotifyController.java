package com.example.music_service.controller;

import com.example.music_service.dto.response.ApiResponse;
import com.example.music_service.service.impl.SpotifyServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/music")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyServiceImpl spotifyService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> loginSpotify(@RequestParam String userId) {
        String loginUrl = spotifyService.buildSpotifyLoginUrl(userId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .code(200)
                .message("Spotify login URL generated successfully")
                .result(loginUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/callback")
    public Mono<ResponseEntity<Void>> handleCallback(@RequestParam String code,
                                                     @RequestParam String state) {
        String userId;
        try {
            userId = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            URI redirectUri = URI.create("http://localhost:5173/music?status=error&message=invalid_state");
            ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
            return Mono.just(response);
        }

        return spotifyService.exchangeCodeForToken(code)
                .flatMap(token -> spotifyService.getUserProfile(token.getAccessToken())
                        .flatMap(profileJson -> {
                            try {
                                JsonNode node = new ObjectMapper().readTree(profileJson);
                                String spotifyUserId = node.get("id").asText();
                                spotifyService.linkSpotifyToUser(userId, spotifyUserId, token);

                                String encodedUser = URLEncoder.encode(userId, StandardCharsets.UTF_8);
                                URI redirectUri = URI.create("http://localhost:5173/music?status=success&user=" + encodedUser);

                                ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
                                return Mono.just(response);

                            } catch (Exception e) {
                                URI redirectUri = URI.create("http://localhost:5173/music?status=error&message=parse_failed");
                                ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
                                return Mono.just(response);
                            }
                        }))
                .onErrorResume(ex -> {
                    String msg = URLEncoder.encode(ex.getMessage(), StandardCharsets.UTF_8);
                    URI redirectUri = URI.create("http://localhost:5173/music?status=error&message=" + msg);
                    ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
                    return Mono.just(response);
                });
    }

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getAccessToken(@RequestParam String userId) {
        String accessToken = spotifyService.getAccessTokenForUser(userId);
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        return ResponseEntity.ok(response);
    }

}
