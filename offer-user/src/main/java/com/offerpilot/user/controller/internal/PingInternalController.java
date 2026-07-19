package com.offerpilot.user.controller.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调试内部接口 —— 验证内部控制器能否正常工作
 */
@RestController
@RequestMapping("/internal/ping")
@RequiredArgsConstructor
public class PingInternalController {

    @GetMapping
    public String ping() {
        return "pong";
    }

    @GetMapping("/{id}")
    public PingResult getById(@PathVariable Long id) {
        return new PingResult(id, "item-" + id);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class PingResult {
        private Long id;
        private String name;
    }
}
