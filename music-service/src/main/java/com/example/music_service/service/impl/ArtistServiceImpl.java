package com.example.music_service.service.impl;

import com.example.music_service.service.interfaces.IArtistService;
import com.example.music_service.dto.spotify.ArtistDto;
import com.example.music_service.entity.Artist;
import com.example.music_service.entity.Genre;
import com.example.music_service.repository.ArtistRepository;
import com.example.music_service.service.interfaces.IGenreService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ArtistServiceImpl implements IArtistService {
    private final ArtistRepository artistRepository;
    private final IGenreService genreSyncService;

    public ArtistServiceImpl(ArtistRepository artistRepository, IGenreService genreSyncService) {
        this.artistRepository = artistRepository;
        this.genreSyncService = genreSyncService;
    }

    public Artist syncArtist(ArtistDto dto) {
        Artist artist = artistRepository.findById(dto.getId()).orElse(new Artist());
        artist.setArtistId(dto.getId());
        artist.setName(dto.getName());
        // Cập nhật dòng này để lấy imageUrl
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            // Lấy URL của hình ảnh đầu tiên trong danh sách
            artist.setImageUrl(dto.getImages().get(0).getUrl());
        } else {
            // Đặt null hoặc một URL mặc định nếu không có hình ảnh
            artist.setImageUrl(null);
        }
        artist.setUri(dto.getUri());
        artist.setHref(dto.getHref());
        artist.setExternalUrls(dto.getExternal_urls() != null ? dto.getExternal_urls().getSpotify() : null);
        artist.setPopularity(dto.getPopularity());
        artist.setFollowersTotal(
                dto.getFollowers() != null ? dto.getFollowers().getTotal() : 0
        );
        Set<Genre> syncedGenres = new HashSet<>();
        if (dto.getGenres() != null) {
            for (String genreName : dto.getGenres()) {
                Genre genre = genreSyncService.syncGenre(genreName);
                syncedGenres.add(genre);
            }
        }
        artist.setGenres(syncedGenres);
        return artistRepository.save(artist);
    }
}
