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

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProperties aiProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * AI 对话——调用 DeepSeek Anthropic 兼容接口
     */
    public String chat(String message) {
        log.info("AI 请求: model={}, message={}", aiProperties.getModel(), message);

        // 1. 构建请求体（Anthropic 格式）
        JSONObject body = new JSONObject();
        body.set("model", aiProperties.getModel());
        body.set("max_tokens", 1024);

        JSONArray messages = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", message);
        messages.add(userMsg);
        body.set("messages", messages);

        // 2. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", aiProperties.getApiKey());
        headers.set("anthropic-version", "2023-06-01");

        // 3. 发送 POST 请求
        String url = aiProperties.getBaseUrl() + "/v1/messages";
        HttpEntity<String> request = new HttpEntity<>(JSONUtil.toJsonStr(body), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.debug("AI 响应: {}", response.getBody());

            // 4. 解析响应（Anthropic 格式—可能有 thinking 和 text 两种块）
            JSONObject responseBody = JSONUtil.parseObj(response.getBody());
            JSONArray contentArray = responseBody.getJSONArray("content");
            if (contentArray != null && !contentArray.isEmpty()) {
                // 找到第一个 type=text 的块（跳过 thinking 块）
                for (Object item : contentArray) {
                    JSONObject block = (JSONObject) item;
                    if ("text".equals(block.getStr("type"))) {
                        return block.getStr("text");
                    }
                }
            }
            return "AI 返回了空结果";
        } catch (Exception e) {
            log.error("AI API 调用失败: {}", e.getMessage());
            return "抱歉，AI 服务暂时不可用（" + e.getMessage() + "）";
        }
    }
}