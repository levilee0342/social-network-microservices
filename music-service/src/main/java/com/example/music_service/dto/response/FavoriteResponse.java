package com.example.music_service.dto.response;

import com.example.music_service.dto.spotify.TrackDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoriteResponse {
    private Long favoriteId;
    private Long addedAt;
    private TrackResponse track;
}
