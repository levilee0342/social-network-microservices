package com.example.post_service.repository;

import com.example.post_service.entity.PostEvent;
import com.example.post_service.enums.PostEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostEventRepository extends JpaRepository<PostEvent, Long> {

    List<PostEvent> findByPostIdOrderByOccurredAtDesc(Long postId);

    List<PostEvent> findByUserIdOrderByOccurredAtDesc(String userId);

    List<PostEvent> findByEventTypeOrderByOccurredAtDesc(PostEventType eventType);

    Page<PostEvent> findByPostIdOrderByOccurredAtDesc(Long postId, Pageable pageable);

    @Query("SELECT pe FROM PostEvent pe WHERE pe.occurredAt BETWEEN ?1 AND ?2 ORDER BY pe.occurredAt DESC")
    List<PostEvent> findEventsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT pe FROM PostEvent pe WHERE pe.postId = ?1 AND pe.eventType = ?2 ORDER BY pe.occurredAt DESC")
    List<PostEvent> findByPostIdAndEventType(Long postId, PostEventType eventType);
}
