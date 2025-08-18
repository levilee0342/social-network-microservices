package com.example.music_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TrackSimpleResponse {
    private String trackId;
    private String name;
    private String previewUrl;
    private Integer durationMs;
    private String albumName;
    private List<String> artistNames;
}

