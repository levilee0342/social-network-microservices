package com.example.music_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlaylistResponse {
    private String playlistId;
    private String name;
    private String imageUrl;
    private String externalUrls;
    private String description;
    private boolean collaborative;
    private String snapshotId;
    private int followers;
    private String href;
    private String uri;
    private List<TrackSimpleResponse> tracks; // Hoặc TrackResponse nếu chi tiết
}
