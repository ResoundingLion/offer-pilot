package com.offerpilot.ai.agent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 工具定义提供者 —— 定义 AI Agent 可调用的工具（JSON Schema 格式）
 * <p>
 * 每个工具对应一个 Feign 调用，LLM 根据用户意图自主选择调用哪些工具。
 */
@Component
public class ToolDefinitionProvider {

    /**
     * 返回给 LLM 的所有工具定义
     */
    public List<Map<String, Object>> getToolDefinitions() {
        return List.of(
                getActiveResumeTool(),
                getDashboardStatsTool(),
                getApplicationsTool(),
                getInterviewsTool(),
                getOfferTool()
        );
    }

    // ==================== 工具定义 ====================

    /**
     * 获取当前简历
     */
    private Map<String, Object> getActiveResumeTool() {
        return Map.of(
                "name", "get_active_resume",
                "description", "获取用户当前使用的简历内容，包含技能、项目经历、教育背景等信息。用于 JD 技能匹配度分析和面试准备。",
                "input_schema", Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                )
        );
    }

    /**
     * 获取投递统计数据
     */
    private Map<String, Object> getDashboardStatsTool() {
        return Map.of(
                "name", "get_dashboard_stats",
                "description", "获取用户的投递统计数据概览：总投递数、进行中数量、面试数、Offer数、近14天每日投递趋势、各渠道来源分布。用于整体求职进展分析。",
                "input_schema", Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                )
        );
    }

    /**
     * 获取投递列表
     */
    private Map<String, Object> getApplicationsTool() {
        return Map.of(
                "name", "get_applications",
                "description", "获取用户的全部投递记录列表，包含每个投递的公司ID、岗位ID、状态（已投递/测评/笔试/面试/Offer/被拒/放弃）、投递渠道、日期等。用于分析投递分布和拒信原因。",
                "input_schema", Map.of(
                        "type", "object",
                        "properties", Map.of(),
                        "required", List.of()
                )
        );
    }

    /**
     * 获取面试记录
     */
    private Map<String, Object> getInterviewsTool() {
        return Map.of(
                "name", "get_interviews",
                "description", "获取某次投递的面试记录列表，包含面试轮次（一面/二面/三面/四面/HR面）、面试时间、面试类型、面试官、结果（待定/通过/不通过）和反馈内容。用于分析面试表现和拒信原因。",
                "input_schema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "applicationId", Map.of(
                                        "type", "number",
                                        "description", "投递记录 ID"
                                )
                        ),
                        "required", List.of("applicationId")
                )
        );
    }

    /**
     * 获取 Offer
     */
    private Map<String, Object> getOfferTool() {
        return Map.of(
                "name", "get_offer",
                "description", "获取某次投递的 Offer 详情，包含薪资、奖金、股票、福利、截止日期、状态（待接受/已接受/已拒绝）和备注。",
                "input_schema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "applicationId", Map.of(
                                        "type", "number",
                                        "description", "投递记录 ID"
                                )
                        ),
                        "required", List.of("applicationId")
                )
        );
    }
}
