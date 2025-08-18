package com.example.music_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "albums")
@Data
public class Album {

    @Id
    @Column(name = "album_id")
    private String albumId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "release_date")
    private String releaseDate;

    @Column(name = "album_type", length = 50)
    private String albumType;

    @Column(name = "total_tracks")
    private Integer totalTracks;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "external_urls")
    private String externalUrls;

    private String href;

    private String uri;
}
