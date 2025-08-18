package com.example.post_service.repository;

import com.example.post_service.entity.PostBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    List<PostBookmark> findByUserId(String userId);
    boolean existsByUserIdAndPost_PostId(String userId, Long postId);
}
