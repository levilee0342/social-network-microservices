package com.example.music_service.service.impl;

import com.example.music_service.service.interfaces.IAlbumService;
import com.example.music_service.service.interfaces.IArtistService;
import com.example.music_service.service.interfaces.ITrackService;
import com.example.music_service.dto.spotify.TrackDto;
import com.example.music_service.entity.Album;
import com.example.music_service.entity.Artist;
import com.example.music_service.entity.Track;
import com.example.music_service.repository.TrackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrackServiceImpl implements ITrackService {
    private final TrackRepository trackRepository;
    private final IAlbumService albumSyncService;
    private final IArtistService artistSyncService;

    public TrackServiceImpl(TrackRepository trackRepository,
                            IAlbumService albumSyncService,
                            IArtistService artistSyncService) {
        this.trackRepository = trackRepository;
        this.albumSyncService = albumSyncService;
        this.artistSyncService = artistSyncService;
    }

    @Transactional
    public Track syncTrack(TrackDto dto) {
        Track track = trackRepository.findById(dto.getId()).orElse(new Track());
        track.setTrackId(dto.getId());

        track.setName(dto.getName());
        track.setDurationMs(dto.getDuration_ms());
        track.setExplicit(dto.isExplicit());
        track.setPopularity(dto.getPopularity());
        track.setPreviewUrl(dto.getPreview_url());
        track.setTrackNumber(dto.getTrack_number());
        track.setUri(dto.getUri());
        track.setHref(dto.getHref());
        track.setExternalUrls(dto.getExternal_urls() != null ? dto.getExternal_urls().getSpotify() : null);
        track.setIsPlayable(true); // hoặc tùy vào DTO nếu có field này

        // Sync album
        Album album = albumSyncService.syncAlbum(dto.getAlbum());
        track.setAlbum(album);

        // Sync artists
        Set<Artist> syncedArtists = new HashSet<>();
        for (var artistDto : dto.getArtists()) {
            Artist artist = artistSyncService.syncArtist(artistDto);
            syncedArtists.add(artist);
        }
        track.setArtists(syncedArtists);

        return trackRepository.save(track);
    }

    public TrackDto getTrackById(String id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Track not found with id: " + id));

        return TrackDto.builder()
                .id(track.getTrackId())
                .name(track.getName())
                .duration_ms(track.getDurationMs())
                .preview_url(track.getPreviewUrl())
                .build();
    }
}
