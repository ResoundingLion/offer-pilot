package com.offerpilot.ai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.offerpilot")
@EnableFeignClients(basePackages = "com.offerpilot.api.client")
@MapperScan("com.offerpilot.ai.mapper")
public class OfferPilotAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OfferPilotAiApplication.class, args);
    }
}
