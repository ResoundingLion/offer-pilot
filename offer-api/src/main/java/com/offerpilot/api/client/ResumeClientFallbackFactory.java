package com.offerpilot.api.client;

import com.offerpilot.api.dto.ResumeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResumeClientFallbackFactory implements FallbackFactory<ResumeClient> {

    @Override
    public ResumeClient create(Throwable cause) {
        log.warn("Feign [resumeClient] 调用降级: {}", cause.getMessage());
        return userId -> {
            log.debug("ResumeClient 降级返回 null, userId={}", userId);
            return null;
        };
    }
}
