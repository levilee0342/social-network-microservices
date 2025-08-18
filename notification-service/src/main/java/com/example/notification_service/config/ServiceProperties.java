package com.example.notification_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceProperties {
    private ServiceConfig identity;
    private ServiceConfig profile;
    private ServiceConfig search;
    private ServiceConfig post;

    @Data
    public static class ServiceConfig {
        private String url;
    }

    @PostConstruct
    public void init() {
        System.out.println("ServiceProperties initialized: ");
        System.out.println("Identity: " + (identity != null ? identity.getUrl() : "null"));
        System.out.println("Profile: " + (profile != null ? profile.getUrl() : "null"));
    }
}
