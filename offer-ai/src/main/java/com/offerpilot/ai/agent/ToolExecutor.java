package com.offerpilot.ai.agent;

import cn.hutool.json.JSONUtil;
import com.offerpilot.api.client.ApplicationClient;
import com.offerpilot.api.client.InterviewClient;
import com.offerpilot.api.client.OfferClient;
import com.offerpilot.api.client.ResumeClient;
import com.offerpilot.api.dto.OfferDTO;
import com.offerpilot.api.dto.ResumeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工具执行器 —— 根据 LLM 选择的工具名，执行对应的 Feign 调用
 * <p>
 * 每个工具名映射到一个 Feign Client 方法，结果转 JSON 字符串返回 LLM。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ResumeClient resumeClient;
    private final ApplicationClient applicationClient;
    private final InterviewClient interviewClient;
    private final OfferClient offerClient;

    /**
     * 执行工具调用
     *
     * @param toolName 工具名
     * @param args     工具参数（LLM 传来的 JSON）
     * @param userId   当前用户 ID（由 AgentService 传入）
     * @return 工具执行结果的 JSON 字符串
     */
    public String execute(String toolName, Map<String, Object> args, Long userId) {
        log.info("执行工具: toolName={}, args={}, userId={}", toolName, args, userId);

        try {
            return switch (toolName) {
                case "get_active_resume" -> executeGetActiveResume(userId);
                case "get_dashboard_stats" -> executeGetDashboardStats(userId);
                case "get_applications" -> executeGetApplications(userId);
                case "get_interviews" -> executeGetInterviews(args);
                case "get_offer" -> executeGetOffer(args);
                default -> "{\"error\": \"未知工具: " + toolName + "\"}";
            };
        } catch (Exception e) {
            log.error("工具执行失败: toolName={}", toolName, e);
            return "{\"error\": \"工具执行异常: " + e.getMessage() + "\"}";
        }
    }

    // ==================== 工具执行方法 ====================

    /** 获取当前简历 */
    private String executeGetActiveResume(Long userId) {
        ResumeDTO resume = resumeClient.getActiveResume(userId);
        if (resume == null) {
            return "{\"message\": \"用户还没有创建简历或被拒绝的简历\"}";
        }
        return JSONUtil.toJsonStr(resume);
    }

    /** 获取 Dashboard 统计 */
    private String executeGetDashboardStats(Long userId) {
        Map<String, Object> stats = applicationClient.getDashboardStats(userId);
        if (stats == null || stats.isEmpty()) {
            return "{\"message\": \"暂无投递数据\"}";
        }
        return JSONUtil.toJsonStr(stats);
    }

    /** 获取投递列表 */
    private String executeGetApplications(Long userId) {
        var apps = applicationClient.getApplications(userId);
        if (apps == null || apps.isEmpty()) {
            return "{\"message\": \"暂无投递记录\"}";
        }
        return JSONUtil.toJsonStr(apps);
    }

    /** 获取面试记录 */
    private String executeGetInterviews(Map<String, Object> args) {
        Long applicationId = getLongArg(args, "applicationId");
        if (applicationId == null) {
            return "{\"error\": \"缺少参数: applicationId\"}";
        }
        var interviews = interviewClient.getInterviews(applicationId);
        if (interviews == null || interviews.isEmpty()) {
            return "{\"message\": \"该投递暂无面试记录\"}";
        }
        return JSONUtil.toJsonStr(interviews);
    }

    /** 获取 Offer */
    private String executeGetOffer(Map<String, Object> args) {
        Long applicationId = getLongArg(args, "applicationId");
        if (applicationId == null) {
            return "{\"error\": \"缺少参数: applicationId\"}";
        }
        OfferDTO offer = offerClient.getOffer(applicationId);
        if (offer == null) {
            return "{\"message\": \"该投递暂无 Offer\"}";
        }
        return JSONUtil.toJsonStr(offer);
    }

    // ==================== 工具方法 ====================

    /** 从 Map 参数中安全获取 Long 值（兼容 Integer 和 Long 类型） */
    private Long getLongArg(Map<String, Object> args, String key) {
        Object val = args.get(key);
        if (val == null) return null;
        if (val instanceof Number num) return num.longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
