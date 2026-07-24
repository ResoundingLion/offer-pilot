package com.offerpilot.api.client;

import com.offerpilot.api.dto.PositionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "offer-user", contextId = "positionClient", path = "/internal/positions",
        fallbackFactory = PositionClientFallbackFactory.class)
public interface PositionClient {

    @GetMapping("/{id}")
    PositionDTO getPositionById(@PathVariable("id") Long id);
}
