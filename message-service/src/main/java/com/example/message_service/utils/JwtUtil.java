package com.example.message_service.utils;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.accessTokenExpiration:3600000}") // Mặc định 1 giờ (3600s)
    private long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration:604800000}") // Mặc định 7 ngày (7*24*3600s)
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secret);
            if (decodedKey.length < 32) { // Đảm bảo key đủ dài cho HS256
                throw new IllegalArgumentException("JWT secret key must be at least 256 bits (32 bytes) after Base64 decoding");
            }
            this.key = Keys.hmacShaKeyFor(decodedKey);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to decode JWT secret key: {}", e.getMessage());
            throw new IllegalStateException("Invalid JWT secret key configuration", e);
        }
    }

    // Phương thức cho backward compatibility với login trong AuthService
    public String generateToken(String userId) {
        return generateAccessToken(userId); // Chuyển hướng đến generateAccessToken
    }

    public String generateAccessToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            logger.warn("Failed to extract username from token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }


    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException e) {
            logger.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }





}