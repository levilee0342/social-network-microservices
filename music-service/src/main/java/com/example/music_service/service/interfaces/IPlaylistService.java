package com.example.music_service.service.interfaces;

import com.example.music_service.dto.response.PlaylistResponse;
import com.example.music_service.dto.spotify.PlaylistDto;
import com.example.music_service.entity.Playlist;

public interface IPlaylistService {
    Playlist syncPlaylist(PlaylistDto dto);
    PlaylistResponse getPlaylistByIdWithTracks(String playlistId, int page, int size);
}
