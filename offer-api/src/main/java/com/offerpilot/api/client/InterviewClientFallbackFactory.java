package com.offerpilot.api.client;

import com.offerpilot.api.dto.InterviewDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class InterviewClientFallbackFactory implements FallbackFactory<InterviewClient> {

    @Override
    public InterviewClient create(Throwable cause) {
        log.warn("Feign [interviewClient] 调用降级: {}", cause.getMessage());
        return applicationId -> {
            log.debug("InterviewClient 降级返回空列表, applicationId={}", applicationId);
            return Collections.emptyList();
        };
    }
}
