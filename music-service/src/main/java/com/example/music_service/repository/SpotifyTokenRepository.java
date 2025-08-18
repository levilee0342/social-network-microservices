package com.example.music_service.repository;

import com.example.music_service.entity.SpotifyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpotifyTokenRepository extends JpaRepository<SpotifyToken, Long> {
}


