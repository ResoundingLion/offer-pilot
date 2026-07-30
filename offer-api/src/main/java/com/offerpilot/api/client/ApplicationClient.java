package com.offerpilot.api.client;

import com.offerpilot.api.dto.ApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 投递 Feign 客户端 —— 调用 offer-application 的内部接口
 */
@FeignClient(name = "offer-application", contextId = "applicationClient", path = "/internal/applications",
        fallbackFactory = ApplicationClientFallbackFactory.class)
public interface ApplicationClient {

    /**
     * 获取用户全部投递记录
     */
    @GetMapping
    List<ApplicationDTO> getApplications(@RequestParam("userId") Long userId);

    /**
     * 获取 Dashboard 统计数据
     */
    @GetMapping("/dashboard/stats")
    Map<String, Object> getDashboardStats(@RequestParam("userId") Long userId);
}
