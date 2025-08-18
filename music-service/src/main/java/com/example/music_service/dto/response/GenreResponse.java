package com.example.music_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreResponse {
    private Long genreId;
    private String name;
}
