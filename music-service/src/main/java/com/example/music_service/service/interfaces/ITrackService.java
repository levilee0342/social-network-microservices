package com.example.music_service.service.interfaces;

import com.example.music_service.dto.spotify.TrackDto;
import com.example.music_service.entity.Track;

import java.util.List;

public interface ITrackService {
    Track syncTrack(TrackDto dto);
    TrackDto getTrackById(String id);
}
