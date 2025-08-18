package com.example.music_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tracks")
@Data
public class Track {

    @Id
    @Column(name = "track_id")
    private String trackId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "preview_url", columnDefinition = "TEXT")
    private String previewUrl;

    @Column(name = "explicit")
    private Boolean explicit;

    @Column(name = "is_playable")
    private Boolean isPlayable;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "popularity")
    private Integer popularity;

    @Column(name = "external_urls")
    private String externalUrls;

    private String uri;

    private String href;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToMany
    @JoinTable(
            name = "track_artists",
            joinColumns = @JoinColumn(name = "track_id"),
            inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    private Set<Artist> artists = new HashSet<>();

}
