package com.example.music_service.util;

import com.example.music_service.dto.spotify.ArtistDto;
import com.example.music_service.dto.spotify.PlaylistDto;
import com.example.music_service.dto.spotify.TrackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SpotifyApiClient {

    private static final String BASE_URL = "https://api.spotify.com/v1";
    private final RestTemplate restTemplate = new RestTemplate(); // hoặc inject qua @Bean

    public TrackDto getTrackById(String trackId, String accessToken) {
        String endpoint = "/tracks/" + trackId;
        return makeGetRequest(endpoint, accessToken, TrackDto.class);
    }

    public ArtistDto getArtistById(String artistId, String accessToken) {
        String endpoint = "/artists/" + artistId;
        return makeGetRequest(endpoint, accessToken, ArtistDto.class);
    }

    public PlaylistDto getPlaylistById(String playlistId, String accessToken) {
        String endpoint = "/playlists/" + playlistId;
        return makeGetRequest(endpoint, accessToken, PlaylistDto.class);
    }

    private <T> T makeGetRequest(String endpoint, String accessToken, Class<T> responseType) {
        String url = BASE_URL + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<T> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                responseType
        );
        return response.getBody();
    }
}

