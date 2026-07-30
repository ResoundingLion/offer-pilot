package com.offerpilot.api.client;

import com.offerpilot.api.dto.ResumeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 简历 Feign 客户端 —— 调用 offer-user 的内部接口
 */
@FeignClient(name = "offer-user", contextId = "resumeClient", path = "/internal/resumes",
        fallbackFactory = ResumeClientFallbackFactory.class)
public interface ResumeClient {

    /**
     * 获取用户当前使用的简历（含 contentText，供 AI 分析）
     */
    @GetMapping("/active")
    ResumeDTO getActiveResume(@RequestParam("userId") Long userId);
}
