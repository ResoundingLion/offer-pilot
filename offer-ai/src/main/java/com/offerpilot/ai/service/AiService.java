package com.offerpilot.ai.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.offerpilot.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    // ==================== 旧版 API（保留兼容） ====================

    /**
     * AI 对话——调用 DeepSeek Anthropic 兼容接口（纯文本，无工具）
     */
    public String chat(String message) {
        log.info("AI 请求: model={}, message={}", aiProperties.getModel(), message);

        JSONObject body = new JSONObject();
        body.set("model", aiProperties.getModel());
        body.set("max_tokens", 1024);

        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", message);
        messages.add(userMsg);
        body.set("messages", messages);

        String jsonResponse = postToLlm(body);
        return extractTextFromResponse(jsonResponse);
    }

    // ==================== Agent API（支持工具调用） ====================

    /**
     * AI Agent 请求——支持 system prompt + 多轮消息 + 工具定义
     * <p>
     * 返回 LLM 原始响应，由 AgentService 决定是工具调用还是文本回复。
     *
     * @param system      系统提示词
     * @param messages    Anthropic 格式的消息列表
     *                    [{role: "user"/"assistant", content: "文本" 或 [{type:"tool_use",...}]}]
     * @param tools       工具定义列表（JSON Schema）
     * @param maxTokens   最大 token 数
     * @return 解析后的响应 Map，包含 key:
     *         - stopReason: "tool_use" / "end_turn" / "max_tokens"
     *         - content: JSONArray of content blocks [{type, id, name, input, text, ...}]
     */
    public Map<String, Object> sendAgentRequest(String system, List<Map<String, Object>> messages,
                                                 List<Map<String, Object>> tools, int maxTokens) {
        // 1. 构建请求体
        JSONObject body = new JSONObject();
        body.set("model", aiProperties.getModel());
        body.set("max_tokens", maxTokens);

        if (system != null && !system.isBlank()) {
            body.set("system", system);
        }

        JSONArray msgArray = new JSONArray();
        for (Map<String, Object> msg : messages) {
            msgArray.add(JSONUtil.parseObj(msg));
        }
        body.set("messages", msgArray);

        if (tools != null && !tools.isEmpty()) {
            JSONArray toolsArray = new JSONArray();
            for (Map<String, Object> tool : tools) {
                toolsArray.add(JSONUtil.parseObj(tool));
            }
            body.set("tools", toolsArray);
        }

        log.debug("Agent LLM 请求: messages={}, tools={}",
                messages.size(), tools != null ? tools.size() : 0);

        // 2. 发送
        String jsonResponse = postToLlm(body);

        // 3. 解析
        JSONObject responseBody = JSONUtil.parseObj(jsonResponse);
        Map<String, Object> result = new HashMap<>();

        // stop_reason: "tool_use" | "end_turn" | "max_tokens" | "stop_sequence"
        result.put("stopReason", responseBody.getStr("stop_reason", ""));

        JSONArray contentArray = responseBody.getJSONArray("content");
        result.put("content", contentArray != null ? contentArray : new JSONArray());

        return result;
    }

    // ==================== 底层 HTTP 调用 ====================

    /** 统一 HTTP POST 到 LLM API */
    private String postToLlm(JSONObject body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", aiProperties.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        String url = aiProperties.getBaseUrl() + "/v1/messages";
        HttpEntity<String> request = new HttpEntity<>(JSONUtil.toJsonStr(body), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("LLM API 调用失败: {}", e.getMessage());
            throw new RuntimeException("AI 服务暂时不可用: " + e.getMessage());
        }
    }

    /** 从 Anthropic 响应中提取第一个 text 块（跳过 thinking 块） */
    private String extractTextFromResponse(String jsonResponse) {
        try {
            JSONObject responseBody = JSONUtil.parseObj(jsonResponse);
            JSONArray contentArray = responseBody.getJSONArray("content");
            if (contentArray != null && !contentArray.isEmpty()) {
                for (Object item : contentArray) {
                    JSONObject block = (JSONObject) item;
                    if ("text".equals(block.getStr("type"))) {
                        return block.getStr("text");
                    }
                }
            }
            return "AI 返回了空结果";
        } catch (Exception e) {
            log.error("解析 AI 响应失败: {}", e.getMessage());
            return "抱歉，解析 AI 响应失败";
        }
    }
}
