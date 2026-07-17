package com.offerpilot.application.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.offerpilot.common.config.OfferPilotMetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 *
 * 显式注册 MetaObjectHandler 的 @Bean，确保自动填充生效。
 * 为什么需要这步？
 *   - OfferPilotMetaObjectHandler 在 offer-common 中有 @Component，
 *     但在某些模块中 @ComponentScan 可能无法从依赖 JAR 中扫描到。
 *   - 显式 @Bean 确保 MetaObjectHandler 一定被注册到 MyBatis-Plus 的 GlobalConfig 中。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new OfferPilotMetaObjectHandler();
    }
}
