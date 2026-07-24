package com.offerpilot.api.client;

import com.offerpilot.api.dto.CompanyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "offer-user", contextId = "companyClient", path = "/internal/companies",
        fallbackFactory = CompanyClientFallbackFactory.class)
public interface CompanyClient {

    @GetMapping("/{id}")
    CompanyDTO getCompanyById(@PathVariable("id") Long id);
}
