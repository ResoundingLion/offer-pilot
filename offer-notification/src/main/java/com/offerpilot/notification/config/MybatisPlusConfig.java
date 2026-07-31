package com.offerpilot.notification.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.offerpilot.common.config.OfferPilotMetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 * <p>
 * 显式注册 MetaObjectHandler 的 @Bean，确保自动填充 createdAt/updatedAt 生效。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new OfferPilotMetaObjectHandler();
    }
}
