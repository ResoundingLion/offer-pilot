package com.offerpilot.ai.controller;

import com.offerpilot.ai.service.AiService;
import com.offerpilot.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * AI 对话接口
     */
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.isBlank()) {
            return Result.badRequest("消息不能为空");
        }
        String reply = aiService.chat(message);
        return Result.success(reply);
    }

    /**
     * 保留测试接口，确认服务存活
     */
    @GetMapping("/test")
    public String test() {
        return "offer-ai 服务启动成功！";
    }
}
