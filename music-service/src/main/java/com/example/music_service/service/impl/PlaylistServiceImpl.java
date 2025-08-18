package com.example.music_service.service.impl;
import com.example.music_service.dto.response.PlaylistResponse;
import com.example.music_service.dto.response.TrackSimpleResponse;
import com.example.music_service.dto.spotify.PlaylistDto;
import com.example.music_service.dto.spotify.TrackDto;
import com.example.music_service.entity.Artist;
import com.example.music_service.entity.Playlist;
import com.example.music_service.entity.Track;
import com.example.music_service.repository.PlaylistRepository;
import com.example.music_service.service.interfaces.IPlaylistService;
import com.example.music_service.service.interfaces.ITrackService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PlaylistServiceImpl implements IPlaylistService {
    private final PlaylistRepository playlistRepository;
    private final ITrackService trackSyncService;

    public PlaylistServiceImpl(PlaylistRepository playlistRepository,
                               ITrackService trackSyncService) {
        this.playlistRepository = playlistRepository;
        this.trackSyncService = trackSyncService;
    }

    @Transactional
    public Playlist syncPlaylist(PlaylistDto dto) {
        Playlist playlist = playlistRepository.findById(dto.getId())
                .orElse(new Playlist());
        playlist.setPlaylistId(dto.getId());
        playlist.setName(dto.getName());
        playlist.setDescription(dto.getDescription());
        playlist.setCollaborative(dto.isCollaborative());
        playlist.setFollowers(dto.getFollowers() != null ? dto.getFollowers().getTotal() : 0);
        playlist.setSnapshotId(dto.getSnapshot_id());
        playlist.setExternalUrls(dto.getExternal_urls() != null ? dto.getExternal_urls().getSpotify() : null);
        playlist.setImageUrl(dto.getImages() != null && !dto.getImages().isEmpty() ? dto.getImages().get(0).getUrl() : null);

        playlist = playlistRepository.save(playlist); // save trước để tạo khóa ngoại

        // Đồng bộ từng track trong playlist
        if (dto.getTracks() != null && dto.getTracks().getItems() != null) {
            Set<Track> playlistTracks = new HashSet<>(); // Sử dụng Set để tránh trùng lặp
            for (PlaylistDto.TrackItem item : dto.getTracks().getItems()) { // <--- Sửa lỗi cú pháp tại đây
                TrackDto trackDto = item.getTrack();
                if (trackDto != null && trackDto.getId() != null) {
                    Track track = trackSyncService.syncTrack(trackDto);
                    track.setPlaylist(playlist);
                    // Track sẽ tự save lại trong trackSyncService hoặc bạn gọi trackRepo.save(track);
                }
            }
        }

        return playlist;
    }

    @Override
    public PlaylistResponse getPlaylistByIdWithTracks(String playlistId, int page, int size) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist not found"));

        List<TrackSimpleResponse> pagedTracks = playlist.getTracks().stream()
                .skip((long) page * size)
                .limit(size)
                .map(track -> {
                    String albumName = track.getAlbum() != null ? track.getAlbum().getName() : null;
                    List<String> artistNames = track.getArtists().stream()
                            .map(Artist::getName)
                            .collect(Collectors.toList());

                    return TrackSimpleResponse.builder()
                            .trackId(track.getTrackId())
                            .name(track.getName())
                            .previewUrl(track.getPreviewUrl())
                            .durationMs(track.getDurationMs())
                            .albumName(albumName)
                            .artistNames(artistNames)
                            .build();
                })
                .collect(Collectors.toList());

        return PlaylistResponse.builder()
                .playlistId(playlist.getPlaylistId())
                .name(playlist.getName())
                .imageUrl(playlist.getImageUrl())
                .externalUrls(playlist.getExternalUrls())
                .description(playlist.getDescription())
                .collaborative(playlist.isCollaborative())
                .snapshotId(playlist.getSnapshotId())
                .followers(playlist.getFollowers())
                .href(playlist.getHref())
                .uri(playlist.getUri())
                .tracks(pagedTracks)
                .build();
    }
}
