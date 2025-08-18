package com.example.music_service.service.interfaces;

import com.example.music_service.dto.spotify.AlbumDto;
import com.example.music_service.entity.Album;

public interface IAlbumService {
    Album syncAlbum(AlbumDto dto);
}
