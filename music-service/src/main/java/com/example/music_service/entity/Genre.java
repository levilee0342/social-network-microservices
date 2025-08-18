package com.example.music_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "genres")
@Data
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private Long genreId;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @ManyToMany(mappedBy = "genres") // mappedBy chỉ ra rằng mối quan hệ được quản lý bởi trường 'genres' trong Artist
    private Set<Artist> artists = new HashSet<>();
}
