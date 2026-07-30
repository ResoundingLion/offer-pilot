package com.offerpilot.api.client;

import com.offerpilot.api.dto.OfferDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OfferClientFallbackFactory implements FallbackFactory<OfferClient> {

    @Override
    public OfferClient create(Throwable cause) {
        log.warn("Feign [offerClient] 调用降级: {}", cause.getMessage());
        return applicationId -> {
            log.debug("OfferClient 降级返回 null, applicationId={}", applicationId);
            return null;
        };
    }
}
