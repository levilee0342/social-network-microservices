package com.example.trending_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trending_posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Float score;

    @Column(name = "trending_time", nullable = false)
    @Builder.Default
    private LocalDateTime trendingTime = LocalDateTime.now();
}

