package com.example.log_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceProperties {
    private ServiceConfig identity;
    private ServiceConfig post;
    private ServiceConfig notification;
    private ServiceConfig search;

    @Data
    public static class ServiceConfig {
        private String url;
    }
}
