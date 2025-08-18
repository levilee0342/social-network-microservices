package com.example.music_service.repository;

import com.example.music_service.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long>{
    Page<Favorite> findByUser_UserId(String userId, Pageable pageable);

    Optional<Favorite> findByUser_UserIdAndTrack_TrackId(String userId, String trackId);
    void deleteByUser_UserIdAndTrack_TrackId(String userId, String trackId);
}
