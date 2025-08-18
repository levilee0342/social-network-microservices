package com.example.post_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post_shares")
@Data
public class PostShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shareId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    @JsonBackReference
    private Post post;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "postShare", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "postShare", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("like-post-share")
    private List<Like> likes = new ArrayList<>();

    @Column(name = "shared_at", nullable = false, updatable = false)
    private Long sharedAt;
}
