package com.example.music_service.service.impl;

import com.example.music_service.dto.response.AlbumResponse;
import com.example.music_service.dto.response.ArtistResponse;
import com.example.music_service.dto.response.FavoriteResponse;
import com.example.music_service.dto.response.TrackResponse;
import com.example.music_service.repository.FavoriteRepository;
import com.example.music_service.repository.TrackRepository;
import com.example.music_service.repository.UserRepository;
import com.example.music_service.service.interfaces.IFavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.example.music_service.entity.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements IFavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TrackRepository trackRepository;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository,
                               UserRepository userRepository,
                               TrackRepository trackRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.trackRepository = trackRepository;
    }

    @Override
    public Favorite addFavorite(String userId, String trackId) {
        if (favoriteRepository.findByUser_UserIdAndTrack_TrackId(userId, trackId).isPresent()) {
            throw new IllegalStateException("Track already in favorites");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new IllegalArgumentException("Track not found"));

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setTrack(track);
        return favoriteRepository.save(favorite);
    }

    @Override
    public void removeFavorite(String userId, String trackId) {
        favoriteRepository.deleteByUser_UserIdAndTrack_TrackId(userId, trackId);
    }

    @Override
    public Page<FavoriteResponse> getFavoritesByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "addedAt"));
        Page<Favorite> favorites = favoriteRepository.findByUser_UserId(userId, pageable);

        return favorites.map(fav -> {
            Track track = fav.getTrack();
            Album album = track.getAlbum();
            Set<Artist> artists = track.getArtists();

            // Build ArtistResponse list
            Set<ArtistResponse> artistResponses = artists.stream()
                    .map(artist -> ArtistResponse.builder()
                            .artistId(artist.getArtistId())
                            .name(artist.getName())
                            .imageUrl(artist.getImageUrl())
                            .followersTotal(artist.getFollowersTotal())
                            .popularity(artist.getPopularity())
                            .externalUrls(artist.getExternalUrls())
                            .href(artist.getHref())
                            .uri(artist.getUri())
                            .genres(artist.getGenres().stream().map(Genre::getName).collect(Collectors.toSet()))
                            .build()
                    ).collect(Collectors.toSet());

            // Build AlbumResponse
            AlbumResponse albumResponse = album != null ? AlbumResponse.builder()
                    .albumId(album.getAlbumId())
                    .name(album.getName())
                    .releaseDate(album.getReleaseDate())
                    .albumType(album.getAlbumType())
                    .totalTracks(album.getTotalTracks())
                    .imageUrl(album.getImageUrl())
                    .externalUrls(album.getExternalUrls())
                    .href(album.getHref())
                    .uri(album.getUri())
                    .build() : null;

            // Build TrackResponse
            TrackResponse trackResponse = TrackResponse.builder()
                    .trackId(track.getTrackId())
                    .name(track.getName())
                    .durationMs(track.getDurationMs())
                    .previewUrl(track.getPreviewUrl())
                    .explicit(track.getExplicit())
                    .isPlayable(track.getIsPlayable())
                    .trackNumber(track.getTrackNumber())
                    .popularity(track.getPopularity())
                    .externalUrls(track.getExternalUrls())
                    .href(track.getHref())
                    .uri(track.getUri())
                    .album(albumResponse)
                    .artists(artistResponses)
                    .build();

            return FavoriteResponse.builder()
                    .favoriteId(fav.getFavoriteId())
                    .addedAt(fav.getAddedAt() != null ? fav.getAddedAt() : null)
                    .track(trackResponse)
                    .build();
        });
    }

}
