package com.example.music_service.dto.spotify;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrackDto {
    private String id;
    private String name;
    private int popularity;
    private String preview_url;
    private int track_number;
    private String uri;
    private int duration_ms;
    private boolean explicit;
    private ExternalUrls external_urls;
    private String href;


    private AlbumDto album;
    private List<ArtistDto> artists;

    @Data
    public static class ExternalUrls {
        private String spotify;
    }
}
