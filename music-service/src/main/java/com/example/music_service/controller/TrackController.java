package com.example.music_service.controller;

import com.example.music_service.util.SpotifyApiClient;
import com.example.music_service.dto.response.ApiResponse;
import com.example.music_service.dto.spotify.TrackDto;
import com.example.music_service.entity.Track;
import com.example.music_service.service.interfaces.ISpotifyService;
import com.example.music_service.service.interfaces.ITrackService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/music")
public class TrackController {

    private final ITrackService ITrackService;
    private final SpotifyApiClient spotifyApiClient;
    private final ISpotifyService ISpotifyService;

    public TrackController(ITrackService ITrackService,
                           SpotifyApiClient spotifyApiClient,
                           ISpotifyService ISpotifyService) {
        this.ITrackService = ITrackService;
        this.spotifyApiClient = spotifyApiClient;
        this.ISpotifyService = ISpotifyService;
    }

    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // hoặc từ auth.getPrincipal() nếu dùng UserDetails
    }

    @PostMapping("/sync/track/{id}")
    public ResponseEntity<ApiResponse<Track>> syncTrackFromSpotify(
            @PathVariable String id){

        String userId = getCurrentUserId();
        String spotifyAccessToken = ISpotifyService.getAccessTokenForUser(userId);
        TrackDto dto = spotifyApiClient.getTrackById(id, spotifyAccessToken);
        // Đồng bộ xuống DB
        Track track = ITrackService.syncTrack(dto);

        ApiResponse<Track> apiResponse = ApiResponse.<Track>builder()
                .code(200)
                .message("Synced track from Spotify successfully")
                .result(track)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/track/{id}")
    public ResponseEntity<ApiResponse<TrackDto>> getTrackById(@PathVariable String id) {
        TrackDto track = ITrackService.getTrackById(id);

        ApiResponse<TrackDto> response = ApiResponse.<TrackDto>builder()
                .code(200)
                .message("Fetched track successfully")
                .result(track)
                .build();

        return ResponseEntity.ok(response);
    }


}
