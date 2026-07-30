package com.offerpilot.api.client;

import com.offerpilot.api.dto.ApplicationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ApplicationClientFallbackFactory implements FallbackFactory<ApplicationClient> {

    @Override
    public ApplicationClient create(Throwable cause) {
        log.warn("Feign [applicationClient] 调用降级: {}", cause.getMessage());
        return new ApplicationClient() {
            @Override
            public List<ApplicationDTO> getApplications(Long userId) {
                log.debug("ApplicationClient 降级返回空列表, userId={}", userId);
                return Collections.emptyList();
            }

            @Override
            public Map<String, Object> getDashboardStats(Long userId) {
                log.debug("ApplicationClient 降级返回空 Map, userId={}", userId);
                return Collections.emptyMap();
            }
        };
    }
}
