package com.example.music_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ArtistResponse {
    private String artistId;
    private String name;
    private String imageUrl;
    private int followersTotal;
    private Integer popularity;
    private String externalUrls;
    private String href;
    private String uri;
    private Set<String> genres; // Trả về tên genre thay vì đối tượng genre
}

