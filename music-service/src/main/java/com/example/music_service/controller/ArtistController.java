package com.example.music_service.controller;

import com.example.music_service.util.SpotifyApiClient;
import com.example.music_service.dto.spotify.ArtistDto;
import com.example.music_service.entity.Artist;
import com.example.music_service.service.interfaces.IArtistService;
import com.example.music_service.service.interfaces.ISpotifyService;
import lombok.RequiredArgsConstructor;
import com.example.music_service.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("music")
public class ArtistController {
    private final ISpotifyService ISpotifyService;
    private final SpotifyApiClient spotifyApiClient;
    private final IArtistService IArtistService;

    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    @PostMapping("/sync/artist/{id}")
    public ResponseEntity<ApiResponse<Artist>> syncArtistFromSpotify(@PathVariable String id){
        String userId = getCurrentUserId();
        String spotifyAccessToken = ISpotifyService.getAccessTokenForUser(userId);

        ArtistDto dto = spotifyApiClient.getArtistById(id, spotifyAccessToken);
        Artist artist = IArtistService.syncArtist(dto);

        ApiResponse<Artist> apiResponse = ApiResponse.<Artist>builder()
                .code(200)
                .message("Synced artist from Spotify successfully")
                .result(artist)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
