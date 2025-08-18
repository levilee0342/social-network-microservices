package com.example.music_service.controller;

import com.example.music_service.dto.response.PlaylistResponse;
import com.example.music_service.util.SpotifyApiClient;
import com.example.music_service.dto.response.ApiResponse;
import com.example.music_service.dto.spotify.PlaylistDto;
import com.example.music_service.entity.Playlist;
import com.example.music_service.service.interfaces.IPlaylistService;
import com.example.music_service.service.interfaces.ISpotifyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/music")
public class PlaylistController {
    private final IPlaylistService playlistService;
    private final ISpotifyService spotifyService;
    private final SpotifyApiClient spotifyApiClient;

    public PlaylistController(IPlaylistService playlistService,
                              ISpotifyService spotifyService,
                              SpotifyApiClient spotifyApiClient) {
        this.playlistService = playlistService;
        this.spotifyService = spotifyService;
        this.spotifyApiClient = spotifyApiClient;
    }

    private final RestTemplate restTemplate = new RestTemplate();

    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @PostMapping("/sync/playlist/{id}")
    public ResponseEntity<ApiResponse<Playlist>> syncPlaylist(@PathVariable String id) {
        String userId = getCurrentUserId();
        String spotifyAccessToken = spotifyService.getAccessTokenForUser(userId);
        PlaylistDto dto = spotifyApiClient.getPlaylistById(id, spotifyAccessToken);
        Playlist playlist = playlistService.syncPlaylist(dto);

        ApiResponse<Playlist> apiResponse = ApiResponse.<Playlist>builder()
                .code(200)
                .message("Sync playlist successfully")
                .result(playlist)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/playlist/{playlistId}")
    public ResponseEntity<ApiResponse<PlaylistResponse>> getPlaylistById(
            @PathVariable String playlistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PlaylistResponse response = playlistService.getPlaylistByIdWithTracks(playlistId, page, size);
        ApiResponse<PlaylistResponse> apiResponse = ApiResponse.<PlaylistResponse>builder()
                .code(200)
                .message("Get playlist successfully")
                .result(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
