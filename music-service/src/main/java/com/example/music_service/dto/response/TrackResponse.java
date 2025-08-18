package com.example.music_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class TrackResponse {
    private String trackId;
    private String name;
    private Integer durationMs;
    private String previewUrl;
    private Boolean explicit;
    private Boolean isPlayable;
    private Integer trackNumber;
    private Integer popularity;
    private String externalUrls;
    private String uri;
    private String href;

    private AlbumResponse album; // Có thể chỉ cần tên hoặc ID nếu tối giản
    private Set<ArtistResponse> artists;
}

