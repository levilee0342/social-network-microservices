package com.example.music_service.service.impl;

import com.example.music_service.entity.Genre;
import com.example.music_service.repository.GenreRepository;
import com.example.music_service.service.interfaces.IGenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements IGenreService {

    private final GenreRepository genreRepository;

    public Genre syncGenre(String name) {
        return genreRepository.findByName(name)
                .orElseGet(() -> {
                    Genre genre = new Genre();
                    genre.setName(name);
                    return genreRepository.save(genre);
                });
    }
}