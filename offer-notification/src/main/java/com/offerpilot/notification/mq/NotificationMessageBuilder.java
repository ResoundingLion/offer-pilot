package com.offerpilot.notification.mq;

import com.offerpilot.api.event.ApplicationEvent;

import java.util.List;

/**
 * 通知文案生成器
 * <p>
 * 纯函数：输入状态变更事件 → 输出 [标题, 内容]，无副作用，便于单元测试。
 * 按变更后状态（newStatus）生成不同文案，公司/岗位信息来自事件体（生产者已组装）。
 */
public final class NotificationMessageBuilder {

    private NotificationMessageBuilder() {
    }

    /** 通知类型：投递状态变更 */
    public static final String TYPE_STATUS_CHANGE = "STATUS_CHANGE";

    /**
     * 生成 [标题, 内容]
     */
    public static List<String> build(ApplicationEvent event) {
        String who = describe(event);
        String status = event.getNewStatus() != null ? event.getNewStatus() : "";

        return switch (status) {
            case "APPLIED" -> List.of("投递成功", who + " 已投递，祝好运！");
            case "ONLINE_ASSESSMENT" -> List.of("进展更新", who + " 进入测评/笔试阶段");
            case "INTERVIEW" -> List.of("面试安排", who + " 面试进展已更新（" + stage(event) + "）");
            case "HR_INTERVIEW" -> List.of("HR 面试", who + " 进入 HR 面试环节");
            case "OFFER" -> List.of("拿到 Offer 🎉", "恭喜！" + who + " 向你发出了 Offer");
            case "REJECTED" -> List.of("投递未通过", who + " 未通过，别灰心，继续加油");
            case "WITHDRAWN" -> List.of("投递已撤回", who + " 投递已撤回");
            default -> List.of("投递进展", who + " 状态变更为 " + status);
        };
    }

    /**
     * 生成"公司 · 岗位"描述；两者都没有时兜底用投递 ID
     */
    private static String describe(ApplicationEvent event) {
        StringBuilder sb = new StringBuilder();
        if (event.getCompanyName() != null && !event.getCompanyName().isBlank()) {
            sb.append(event.getCompanyName());
        }
        if (event.getPositionTitle() != null && !event.getPositionTitle().isBlank()) {
            if (sb.length() > 0) {
                sb.append(" · ");
            }
            sb.append(event.getPositionTitle());
        }
        if (sb.length() == 0) {
            sb.append("投递 #").append(event.getApplicationId());
        }
        return sb.toString();
    }

    /**
     * 当前流水线阶段（如 INTERVIEW_1），无则返回空字符串
     */
    private static String stage(ApplicationEvent event) {
        return event.getCurrentStage() != null ? event.getCurrentStage() : "";
    }
}
