package com.example.music_service.dto.spotify;

import lombok.Data;

@Data
public class OwnerDto {
    private String id;
    private ExternalUrls external_urls;
    private String uri;
    private String display_name;
    private String href;


    @Data
    public static class ExternalUrls {
        private String spotify;
    }

}
