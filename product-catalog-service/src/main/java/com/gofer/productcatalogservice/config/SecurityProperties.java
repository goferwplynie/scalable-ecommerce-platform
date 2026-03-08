package com.gofer.productcatalogservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "internal.security")
public class SecurityProperties {
    private String InternalApiKey;
    private String jwtSecret;
    private List<String> protectedMethods;
}
