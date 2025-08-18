package com.example.music_service.controller;

import com.example.music_service.dto.response.ApiResponse;
import com.example.music_service.dto.response.FavoriteResponse;
import com.example.music_service.entity.*;
import com.example.music_service.service.interfaces.IFavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/music")
public class FavoriteController {

    private final IFavoriteService favoriteService;

    public FavoriteController(IFavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/favorites")
    public ResponseEntity<ApiResponse<Favorite>> addFavorite(@RequestParam String userId, @RequestParam String trackId) {
        Favorite favorite = favoriteService.addFavorite(userId, trackId);
        ApiResponse<Favorite> apiResponse = ApiResponse.<Favorite>builder()
                .code(200)
                .message("Add track to favorite successfully")
                .result(favorite)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/favorites")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@RequestParam String userId, @RequestParam String trackId) {
        favoriteService.removeFavorite(userId, trackId);
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(204)
                .message("Removed track from favorites successfully")
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
    }

    @GetMapping("/favorites/{userId}")
    public ResponseEntity<ApiResponse<Page<FavoriteResponse>>> getFavorites(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<FavoriteResponse> favorites = favoriteService.getFavoritesByUser(userId, page, size);
        ApiResponse<Page<FavoriteResponse>> apiResponse = ApiResponse.<Page<FavoriteResponse>>builder()
                .code(200)
                .message("Fetched favorite tracks")
                .result(favorites)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}

