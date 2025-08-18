package com.example.trending_service.repository;

import com.example.trending_service.entity.TrendingEvents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrendingEventRepository extends JpaRepository<TrendingEvents, Long> {
    Optional<TrendingEvents> findFirstByPostId(Long postId);
}

