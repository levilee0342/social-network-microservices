package com.example.post_service.repository;

import com.example.post_service.entity.PostShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostShareRepository extends JpaRepository<PostShare, Long> {
    List<PostShare> findByUserId(String userId);
    boolean existsByUserIdAndPost_PostId(String userId, Long postId);
}

