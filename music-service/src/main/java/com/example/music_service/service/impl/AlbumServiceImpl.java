package com.example.music_service.service.impl;

import com.example.music_service.service.interfaces.IAlbumService;
import com.example.music_service.dto.spotify.AlbumDto;
import com.example.music_service.entity.Album;
import com.example.music_service.repository.AlbumRepository;
import org.springframework.stereotype.Service;

@Service
public class AlbumServiceImpl implements IAlbumService {
    private final AlbumRepository albumRepository;

    public AlbumServiceImpl(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public Album syncAlbum(AlbumDto dto) {
        Album album = albumRepository.findById(dto.getId()).orElse(new Album());
        album.setAlbumId(dto.getId());
        album.setName(dto.getName());
        album.setUri(dto.getUri());
        album.setHref(dto.getHref());
        album.setExternalUrls(dto.getExternal_urls() != null ? dto.getExternal_urls().getSpotify() : null);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            // Lấy URL của hình ảnh đầu tiên trong danh sách
            album.setImageUrl(dto.getImages().get(0).getUrl());
        } else {
            // Đặt null hoặc một URL mặc định nếu không có hình ảnh
            album.setImageUrl(null);
        }
        album.setReleaseDate(dto.getRelease_date());
        album.setTotalTracks(dto.getTotal_tracks());
        album.setAlbumType(dto.getAlbum_type());

        return albumRepository.save(album);
    }
}
