package com.offerpilot.api.client;

import com.offerpilot.api.dto.OfferDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Offer Feign 客户端 —— 调用 offer-application 的内部接口
 */
@FeignClient(name = "offer-application", contextId = "offerClient", path = "/internal/offers",
        fallbackFactory = OfferClientFallbackFactory.class)
public interface OfferClient {

    /**
     * 获取某投递的 Offer
     */
    @GetMapping
    OfferDTO getOffer(@RequestParam("applicationId") Long applicationId);
}
