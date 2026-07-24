package com.offerpilot.api.client;

import com.offerpilot.api.dto.PositionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * PositionClient 熔断降级工厂
 *
 * 当 offer-user 服务不可用或熔断器打开时，返回 null。
 * null 会被 CacheService 以 NullValue（60s TTL）缓存，
 * 后续请求直接返回友好文案 "(⏳ 加载失败)"，不阻塞等待。
 */
@Slf4j
@Component
public class PositionClientFallbackFactory implements FallbackFactory<PositionClient> {

    @Override
    public PositionClient create(Throwable cause) {
        log.warn("Feign [positionClient] 调用降级，cause: {}", cause.getMessage());
        return id -> {
            log.debug("PositionClient 降级返回 null, positionId={}", id);
            return null;
        };
    }
}
