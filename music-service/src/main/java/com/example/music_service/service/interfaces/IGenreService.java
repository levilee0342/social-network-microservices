package com.example.music_service.service.interfaces;

import com.example.music_service.entity.Genre;

public interface IGenreService {
    Genre syncGenre(String name);
}
