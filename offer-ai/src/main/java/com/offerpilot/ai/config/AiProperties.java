package com.offerpilot.ai.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.api")
public class AiProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
}