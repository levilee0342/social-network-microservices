package com.example.music_service.dto.spotify;

import lombok.Data;
import java.util.List;

@Data
public class AlbumDto {
    private String id;
    private String name;
    private int total_tracks;
    private ExternalUrls external_urls;
    private List<Image> images;
    private String release_date;
    private String uri;
    private String href;
    private String album_type;

    private List<TrackDto> tracks;
    private List<ArtistDto> artists;

    @Data
    public static class Image {
        private String url;
        private int height;
        private int width;
    }
    @Data
    public static class ExternalUrls {
        private String spotify;
    }
}
