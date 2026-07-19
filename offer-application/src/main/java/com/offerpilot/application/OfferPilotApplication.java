package com.offerpilot.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.offerpilot")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.offerpilot.api.client")
public class OfferPilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfferPilotApplication.class, args);
    }
}
