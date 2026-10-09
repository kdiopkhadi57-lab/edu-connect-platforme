package com.educonnect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(String uploadDir, List<String> corsOrigins, boolean seedDemoData, Jwt jwt) {

    public record Jwt(String secret, long expirationHours) {
    }
}
