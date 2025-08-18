package com.example.trending_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trending_hashtags")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendingHashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = false)
    private Float score;

    @Column(name = "trending_time", nullable = false)
    @Builder.Default
    private LocalDateTime trendingTime = LocalDateTime.now();
}

