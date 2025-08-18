package com.example.search_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceProperties {
    private ServiceConfig identity;
    private ServiceConfig notification;
    private ServiceConfig post;

    @Data
    public static class ServiceConfig {
        private String url;
    }
}
