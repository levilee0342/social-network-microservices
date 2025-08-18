package com.example.trending_service.repository;

import com.example.trending_service.entity.TrendingPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrendingPostRepository extends JpaRepository<TrendingPost, Long> {
    Optional<TrendingPost> findByPostId(Long postId);
}

