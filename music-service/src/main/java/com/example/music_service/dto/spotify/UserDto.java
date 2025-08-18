package com.example.music_service.dto.spotify;

import lombok.Data;

@Data
public class UserDto {
    private String id;
    private ExternalUrls external_urls;
    private Followers followers;
    private String uri;
    private String href;


    @Data
    public static class Followers {
        private int total;
    }

    @Data
    public static class ExternalUrls {
        private String spotify;
    }
}
