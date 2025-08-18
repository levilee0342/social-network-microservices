package com.example.music_service.dto.spotify;

import lombok.Data;
import java.util.List;

@Data
public class ArtistDto {
    private String id;
    private String name;
    private ExternalUrls external_urls;
    private Followers followers;
    private List<Image> images;
    private List<String> genres;
    private String uri;
    private int popularity;
    private String href;

    @Data
    public static class Followers {
        private int total;
    }

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
