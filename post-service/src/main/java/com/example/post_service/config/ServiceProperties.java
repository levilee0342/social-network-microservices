package com.example.post_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceProperties {
    private ServiceConfig identity;
    private ServiceConfig profile;
    private ServiceConfig notification;
    private ServiceConfig search;
    private ServiceConfig friend;
    private ServiceConfig trending;

    @Data
    public static class ServiceConfig {
        private String url;
    }
}
