package com.example.music_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String userId;
    private String spotifyUserId;
    private String externalUrls;
    private String uri;
    private String href;
}
