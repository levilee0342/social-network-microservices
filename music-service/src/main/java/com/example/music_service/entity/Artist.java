package com.example.music_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "artists")
@Data
public class Artist {

    @Id
    @Column(name = "artist_id")
    private String artistId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "followers_total")
    private int followersTotal;

    @Column(name = "popularity")
    private Integer popularity;

    @Column(name = "external_urls")
    private String externalUrls;

    private String href;

    private String uri;


    @ManyToMany
    @JoinTable(
            name = "artist_genres",
            joinColumns = @JoinColumn(name = "artist_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

}
