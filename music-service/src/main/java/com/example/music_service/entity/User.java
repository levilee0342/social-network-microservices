package com.example.music_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "spotify_user_id", unique = true)
    private String spotifyUserId;

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
    private String refreshToken;

    @Column(name = "token_expiration_time", nullable = false)
    private Long tokenExpirationTime;

    @Column(name = "scope", columnDefinition = "TEXT")
    private String scope;

    @Column(name = "external_urls")
    private String externalUrls;

    private String uri;

    private String href;
}

