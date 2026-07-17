package com.offerpilot.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器（@Component 自动注册，需 @SpringBootApplication 扫描 com.offerpilot）
 */
@Slf4j
public class OfferPilotMetaObjectHandler implements MetaObjectHandler {

    @PostConstruct
    public void init() {
        log.info("OfferPilotMetaObjectHandler 已初始化");
    }

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("insertFill 被调用, entity: {}", metaObject.getOriginalObject().getClass().getSimpleName());
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("updateFill 被调用, entity: {}", metaObject.getOriginalObject().getClass().getSimpleName());
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
