package com.example.music_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "playlists")
@Data
public class Playlist {
    @Id
    @Column(name = "playlist_id")
    private String playlistId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "external_urls")
    private String externalUrls;

    private String description;

    private boolean collaborative;

    @Column(name = "snapshot_id")
    private String snapshotId;

    private int followers;

    private String href;

    private String uri;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true)
    // `mappedBy = "playlist"`: "playlist" là tên trường trong entity Track
    // `cascade = CascadeType.ALL`: Các thao tác như lưu, xóa trên Playlist sẽ ảnh hưởng đến các Track liên quan
    // `orphanRemoval = true`: Nếu một Track bị gỡ khỏi Set này, nó sẽ bị xóa khỏi DB
    private Set<Track> tracks = new HashSet<>();

}
