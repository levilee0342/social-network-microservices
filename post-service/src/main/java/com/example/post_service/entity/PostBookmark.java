package com.example.post_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "post_bookmark")
@Data
public class PostBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonBackReference
    private Post post;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "save_at", nullable = false, updatable = false)
    private Long savedAt;
}
