package com.example.music_service.service.interfaces;

import com.example.music_service.dto.response.FavoriteResponse;
import com.example.music_service.entity.Favorite;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IFavoriteService {
    Favorite addFavorite(String userId, String trackId);
    void removeFavorite(String userId, String trackId);
    Page<FavoriteResponse> getFavoritesByUser(String userId, int page, int size);
}
