package com.example.music_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "spotify_tokens")
@Data
@NoArgsConstructor
public class SpotifyToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "access_token", columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT", nullable = false)
    private String refreshToken;

    @Column(name = "token_type", columnDefinition = "TEXT", nullable = false)
    private String tokenType;

    @Column(name = "expires_in")
    private Long expiresIn;

    public void setExpiresIn(long expiresInSeconds) {
        this.expiresIn = Instant.now().plusSeconds(expiresInSeconds).toEpochMilli();
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= this.expiresIn;
    }
}

