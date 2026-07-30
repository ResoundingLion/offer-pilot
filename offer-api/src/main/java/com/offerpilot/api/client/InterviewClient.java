package com.offerpilot.api.client;

import com.offerpilot.api.dto.InterviewDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 面试 Feign 客户端 —— 调用 offer-application 的内部接口
 */
@FeignClient(name = "offer-application", contextId = "interviewClient", path = "/internal/interviews",
        fallbackFactory = InterviewClientFallbackFactory.class)
public interface InterviewClient {

    /**
     * 获取某投递的所有面试记录
     */
    @GetMapping
    List<InterviewDTO> getInterviews(@RequestParam("applicationId") Long applicationId);
}
