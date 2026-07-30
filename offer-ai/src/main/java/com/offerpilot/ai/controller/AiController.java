package com.offerpilot.ai.controller;

import com.offerpilot.ai.entity.Conversation;
import com.offerpilot.ai.entity.ConversationMessage;
import com.offerpilot.ai.service.AgentService;
import com.offerpilot.ai.service.AiService;
import com.offerpilot.ai.service.ConversationService;
import com.offerpilot.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 服务控制器
 * <p>
 * 提供两种模式：
 * 1. /api/ai/chat —— 旧版简单对话（保留兼容）
 * 2. /api/ai/agent —— Agent 模式（支持工具调用 + 多轮 ReAct）
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AgentService agentService;
    private final ConversationService conversationService;

    // ==================== 旧版 API（保留兼容） ====================

    /**
     * POST /api/ai/chat —— 简单对话（无工具调用）
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

    // ==================== Agent API ====================

    /**
     * POST /api/ai/agent —— Agent 对话（ReAct 多轮工具调用）
     * <p>
     * 请求体：
     * {
     *   "message": "分析我的投递情况",         // 用户消息
     *   "conversationId": 1                   // 续聊时传，新对话不传
     * }
     * <p>
     * 返回：
     * {
     *   "conversationId": 1,
     *   "reply": "分析结果..."
     * }
     */
    @PostMapping("/agent")
    public Result<Map<String, Object>> agent(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") Long userId) {

        String message = (String) request.get("message");
        if (message == null || message.isBlank()) {
            return Result.badRequest("消息不能为空");
        }

        Long conversationId = null;
        if (request.get("conversationId") != null) {
            conversationId = Long.valueOf(request.get("conversationId").toString());
        }

        Map<String, Object> result = agentService.chat(message, userId, conversationId);
        return Result.success(result);
    }

    /**
     * GET /api/ai/conversations —— 获取当前用户的对话列表
     */
    @GetMapping("/conversations")
    public Result<List<Conversation>> getConversations(
            @RequestHeader("X-User-Id") Long userId) {
        List<Conversation> conversations = conversationService.getUserConversations(userId);
        return Result.success(conversations);
    }

    /**
     * DELETE /api/ai/conversations/{id} —— 删除某个对话
     */
    @DeleteMapping("/conversations/{id}")
    public Result<Void> deleteConversation(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        Conversation conv = conversationService.getConversation(id);
        if (conv == null) {
            return Result.notFound();
        }
        if (!conv.getUserId().equals(userId)) {
            return Result.forbidden();
        }
        conversationService.deleteConversation(id);
        return Result.success();
    }

    /**
     * GET /api/ai/conversations/{id}/messages —— 获取对话消息列表
     */
    @GetMapping("/conversations/{id}/messages")
    public Result<List<ConversationMessage>> getConversationMessages(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        Conversation conv = conversationService.getConversation(id);
        if (conv == null) {
            return Result.notFound();
        }
        if (!conv.getUserId().equals(userId)) {
            return Result.forbidden();
        }
        return Result.success(conversationService.getHistory(id));
    }

    // ==================== 存活检测 ====================

    /**
     * GET /api/ai/test —— 服务存活检测（白名单，无需 Token）
     */
    @GetMapping("/test")
    public String test() {
        return "offer-ai 服务启动成功！";
    }
}
