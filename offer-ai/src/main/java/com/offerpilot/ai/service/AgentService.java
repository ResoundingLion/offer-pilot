package com.offerpilot.ai.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.offerpilot.ai.agent.ToolDefinitionProvider;
import com.offerpilot.ai.agent.ToolExecutor;
import com.offerpilot.ai.entity.Conversation;
import com.offerpilot.ai.entity.ConversationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 服务 —— ReAct（Reasoning + Acting）循环核心
 * <p>
 * 工作流程：
 * <pre>
 * for (轮次 < MAX_ROUNDS) {
 *   1. 构建消息列表（系统提示 + 工具定义 + 历史消息 + 当前输入）
 *   2. 调用 LLM
 *   3. 判断 LLM 返回 →
 *      a) tool_use → 执行工具 → 结果追加到对话 → 继续循环
 *      b) 文本 → 返回给用户
 * }
 * </pre>
 * <p>
 * 这就是 Agent 与传统 LLM 调用的本质区别：
 * —— LLM 自主决策"我数据不够，需要再查" → 调工具 → 看结果 → 继续推理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    /** 最大 ReAct 轮数（防止死循环） */
    private static final int MAX_ROUNDS = 5;

    /** Agent 每次调用 LLM 的最大 token 数 */
    private static final int MAX_TOKENS = 4096;

    /**
     * 系统提示词 —— 定义 Agent 的角色、能力和规则
     * <p>
     * LLM 看到这个就知道自己是个求职 Agent，
     * 有哪些工具可以用，什么情况下该用什么工具。
     */
    private static final String SYSTEM_PROMPT = """
            你是一个求职助手 Agent，帮助用户管理求职全流程。

            ## 核心能力

            ### 1. JD 智能分析
            当用户粘贴岗位描述（JD）或询问岗位匹配度时：
            1. 调用 get_active_resume 获取用户简历
            2. 分析 JD 中的技术关键词、岗位方向、难度级别
            3. 与简历中的技能、项目经历做匹配
            4. 给出技能差距分析（matched / missing）和学习建议

            ### 2. AI 投递分析
            当用户询问求职进展或投递情况时：
            1. 先调用 get_dashboard_stats 看整体数据
            2. 如果拒信多，再调 get_applications 分析拒信分布
            3. 如果面试挂的多，进一步调 get_interviews 看面试反馈原因
            4. 综合分析给出优化建议

            ### 3. AI 面试助手
            当用户想准备面试或了解某公司岗位时：
            1. 调 get_active_resume 了解用户技能
            2. 结合常见面试题给出有针对性的准备建议
            3. 3. 注意：面试助手主要依赖你的知识，工具查询到的数据作为补充

            ## 工作规则
            - 始终使用工具查询真实数据——不要凭空编造用户的投递数据
            - 如果工具返回"暂无数据"或报错，如实告知用户
            - 分析结果要结构化、清晰。使用标题、列表、表格等 Markdown 格式
            - 输出使用中文
            - 第一步先理解用户要什么，再选最合适的工具
            """;

    private final AiService aiService;
    private final ToolDefinitionProvider toolDefinitionProvider;
    private final ToolExecutor toolExecutor;
    private final ConversationService conversationService;

    /**
     * Agent 对话入口
     *
     * @param message         用户消息
     * @param userId          当前用户 ID（从网关 X-User-Id 获取）
     * @param conversationId  现有对话 ID（续聊时传入，null 表示新对话）
     * @return 包含 conversationId + reply 的 Map
     */
    public Map<String, Object> chat(String message, Long userId, Long conversationId) {
        // ── 1. 创建或获取对话 ──
        Conversation conv;
        if (conversationId != null) {
            conv = conversationService.getConversation(conversationId);
            if (conv == null) {
                // 对话不存在，创建新的
                conv = conversationService.createConversation(userId, truncateTitle(message));
            } else if (!conv.getUserId().equals(userId)) {
                // 对话不属于当前用户，创建新的
                conv = conversationService.createConversation(userId, truncateTitle(message));
            }
            // 如果第一次对话还没标题，用消息做标题
            if (conv.getTitle() == null || conv.getTitle().isBlank()) {
                conversationService.updateConversationTitle(conv.getId(), truncateTitle(message));
            }
        } else {
            conv = conversationService.createConversation(userId, truncateTitle(message));
        }

        // ── 2. 保存用户消息 ──
        conversationService.addMessage(conv.getId(), "USER", message, null, null, null);

        // ── 3. 加载历史消息并转为 LLM 格式 ──
        List<ConversationMessage> history = conversationService.getHistory(conv.getId());
        List<Map<String, Object>> llmMessages = buildLlmMessages(history);

        // ── 4. 获取工具定义 ──
        List<Map<String, Object>> tools = toolDefinitionProvider.getToolDefinitions();

        // ── 5. ReAct 循环 ──
        for (int round = 0; round < MAX_ROUNDS; round++) {
            log.info("ReAct 轮次 {}/{}: conversationId={}", round + 1, MAX_ROUNDS, conv.getId());

            // 5a. 调用 LLM
            Map<String, Object> response;
            try {
                response = aiService.sendAgentRequest(SYSTEM_PROMPT, llmMessages, tools, MAX_TOKENS);
            } catch (Exception e) {
                log.error("LLM 调用失败", e);
                String errorMsg = "抱歉，AI 服务暂时不可用（" + e.getMessage() + "）";
                conversationService.addMessage(conv.getId(), "ASSISTANT", errorMsg, null, null, null);
                return buildResult(conv.getId(), errorMsg);
            }

            String stopReason = (String) response.getOrDefault("stopReason", "");
            JSONArray contentBlocks = (JSONArray) response.get("content");

            // 5b. 检查是否是工具调用
            if ("tool_use".equals(stopReason) && contentBlocks != null) {
                // 找到第一个 tool_use 块（目前一次只处理一个工具调用）
                JSONObject toolUseBlock = findFirstToolUse(contentBlocks);

                if (toolUseBlock != null) {
                    String toolName = toolUseBlock.getStr("name", "");
                    String toolId = toolUseBlock.getStr("id", "tu_" + System.currentTimeMillis());
                    JSONObject toolArgsObj = toolUseBlock.getJSONObject("input");
                    Map<String, Object> toolArgs = toolArgsObj != null ? toolArgsObj : Map.of();

                    log.info("工具调用: name={}, args={}", toolName, toolArgs);

                    // 保存 TOOL_USE 消息
                    String toolArgsJson = JSONUtil.toJsonStr(toolArgs);
                    conversationService.addMessage(
                            conv.getId(), "TOOL_USE", toolId, toolName, toolArgsJson, null
                    );

                    // 执行工具
                    String toolResult = toolExecutor.execute(toolName, toolArgs, userId);

                    // 保存 TOOL_RESULT 消息
                    conversationService.addMessage(
                            conv.getId(), "TOOL_RESULT", toolId, toolName, null, toolResult
                    );

                    // 把工具调用的消息追加到 LLM 消息列表（用于下一轮）
                    // assistant 返回 tool_use
                    Map<String, Object> assistantMsg = new HashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", List.of(Map.of(
                            "type", "tool_use",
                            "id", toolId,
                            "name", toolName,
                            "input", toolArgs
                    )));
                    llmMessages.add(assistantMsg);

                    // user 返回 tool_result
                    Map<String, Object> toolResultMsg = new HashMap<>();
                    toolResultMsg.put("role", "user");
                    toolResultMsg.put("content", List.of(Map.of(
                            "type", "tool_result",
                            "tool_use_id", toolId,
                            "content", toolResult
                    )));
                    llmMessages.add(toolResultMsg);

                    // 继续下一轮 ReAct
                    continue;
                }
            }

            // 5c. 非工具调用 → 提取文本回复
            String reply = extractTextFromContent(contentBlocks);
            if (reply == null) {
                reply = "抱歉，我没有理解你的问题，能重新说一下吗？";
            }

            // 保存 ASSISTANT 消息
            conversationService.addMessage(conv.getId(), "ASSISTANT", reply, null, null, null);

            return buildResult(conv.getId(), reply);
        }

        // ── 6. 达到最大轮次 ──
        String timeoutMsg = "分析过程较长，请简化一下你的问题，或分多次询问。";
        conversationService.addMessage(conv.getId(), "ASSISTANT", timeoutMsg, null, null, null);
        return buildResult(conv.getId(), timeoutMsg);
    }

    // ==================== 工具方法 ====================

    /** 构建返回给前端的 Map */
    private Map<String, Object> buildResult(Long conversationId, String reply) {
        Map<String, Object> result = new HashMap<>();
        result.put("conversationId", conversationId);
        result.put("reply", reply != null ? reply : "");
        return result;
    }

    /** 截取消息前 30 字作为对话标题 */
    private String truncateTitle(String message) {
        if (message == null) return "新对话";
        return message.length() > 30 ? message.substring(0, 30) + "..." : message;
    }

    /** 从 content 块数组中找第一个 tool_use */
    private JSONObject findFirstToolUse(JSONArray contentBlocks) {
        for (Object item : contentBlocks) {
            JSONObject block = (JSONObject) item;
            if ("tool_use".equals(block.getStr("type"))) {
                return block;
            }
        }
        return null;
    }

    /** 从 content 块数组中提取第一个 text */
    private String extractTextFromContent(JSONArray contentBlocks) {
        if (contentBlocks == null) return null;
        for (Object item : contentBlocks) {
            JSONObject block = (JSONObject) item;
            if ("text".equals(block.getStr("type"))) {
                return block.getStr("text");
            }
        }
        return null;
    }

    /**
     * 将数据库里的对话历史转为 Anthropic Messages API 格式
     * <p>
     * 数据库中的 TOOL_USE 和 TOOL_RESULT 是成对出现的。
     * TOOL_USE → {"role": "assistant", "content": [{type:"tool_use", ...}]}
     * TOOL_RESULT → {"role": "user", "content": [{type:"tool_result", ...}]}
     */
    private List<Map<String, Object>> buildLlmMessages(List<ConversationMessage> history) {
        List<Map<String, Object>> messages = new ArrayList<>();
        String lastToolUseId = null;

        for (ConversationMessage msg : history) {
            switch (msg.getRole()) {
                case "USER":
                    messages.add(Map.of("role", "user", "content",
                            msg.getContent() != null ? msg.getContent() : ""));
                    break;

                case "ASSISTANT":
                    messages.add(Map.of("role", "assistant", "content",
                            msg.getContent() != null ? msg.getContent() : ""));
                    break;

                case "TOOL_USE":
                    lastToolUseId = msg.getContent(); // content 字段存的是 tool_use_id
                    Map<String, Object> toolUse = new HashMap<>();
                    toolUse.put("type", "tool_use");
                    toolUse.put("id", lastToolUseId != null ? lastToolUseId : "tu_" + msg.getId());
                    toolUse.put("name", msg.getToolName());

                    // 解析 tool_args 字符串为 JSON 对象
                    if (msg.getToolArgs() != null) {
                        try {
                            toolUse.put("input", JSONUtil.parseObj(msg.getToolArgs()));
                        } catch (Exception e) {
                            toolUse.put("input", Map.of());
                        }
                    } else {
                        toolUse.put("input", Map.of());
                    }

                    messages.add(Map.of("role", "assistant", "content", List.of(toolUse)));
                    break;

                case "TOOL_RESULT":
                    String tuId = msg.getContent(); // content 存的是对应的 tool_use_id
                    if (tuId == null) tuId = lastToolUseId;
                    if (tuId == null) tuId = "tu_unknown";

                    Map<String, Object> toolResult = new HashMap<>();
                    toolResult.put("type", "tool_result");
                    toolResult.put("tool_use_id", tuId);
                    toolResult.put("content", msg.getToolResult() != null ? msg.getToolResult() : "");

                    messages.add(Map.of("role", "user", "content", List.of(toolResult)));
                    break;
            }
        }

        return messages;
    }
}
