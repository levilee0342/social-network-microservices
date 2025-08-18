package com.example.music_service.service.interfaces;

import com.example.music_service.dto.spotify.ArtistDto;
import com.example.music_service.entity.Artist;

public interface IArtistService {
    Artist syncArtist(ArtistDto dto);
}
