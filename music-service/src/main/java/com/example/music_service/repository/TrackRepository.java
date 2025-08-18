package com.example.music_service.repository;

import com.example.music_service.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, String>{
    List<Track> findAll();
}
