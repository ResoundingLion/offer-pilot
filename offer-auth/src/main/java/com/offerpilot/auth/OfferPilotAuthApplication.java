package com.offerpilot.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.offerpilot")
@EnableDiscoveryClient
public class OfferPilotAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfferPilotAuthApplication.class, args);
    }
}
