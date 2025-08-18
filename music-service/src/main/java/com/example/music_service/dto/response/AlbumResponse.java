package com.example.music_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlbumResponse {
    private String albumId;
    private String name;
    private String releaseDate;
    private String albumType;
    private Integer totalTracks;
    private String imageUrl;
    private String externalUrls;
    private String href;
    private String uri;
}