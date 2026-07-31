package com.offerpilot.notification;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通知服务启动类
 * <p>
 * scanBasePackages="com.offerpilot" 保证 offer-common 中的
 * MetaObjectHandler / GlobalExceptionHandler 等组件被扫描到。
 */
@SpringBootApplication(scanBasePackages = "com.offerpilot")
@MapperScan("com.offerpilot.notification.mapper")
public class OfferPilotNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(OfferPilotNotificationApplication.class, args);
    }
}
