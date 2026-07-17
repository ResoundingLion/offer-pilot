package com.offerpilot.user.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.offerpilot.common.config.OfferPilotMetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * 显式注册 MetaObjectHandler @Bean，确保自动填充生效。
 * 所有模块统一用此方式，不使用 @Component 扫描。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new OfferPilotMetaObjectHandler();
    }
}
