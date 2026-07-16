package com.offerpilot.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "offerpilot.jwt")
public class JwtProperties {

    private String secret;
}
