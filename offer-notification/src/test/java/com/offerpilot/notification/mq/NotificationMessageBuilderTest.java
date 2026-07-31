package com.offerpilot.notification.mq;

import com.offerpilot.api.event.ApplicationEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationMessageBuilder 单元测试
 * <p>
 * 纯函数测试：每个状态分支 → 生成 [标题, 内容]；无公司/岗位时兜底。
 */
class NotificationMessageBuilderTest {

    private ApplicationEvent event(String status, String stage, String company, String position) {
        return ApplicationEvent.builder()
                .applicationId(1L)
                .userId(3L)
                .newStatus(status)
                .currentStage(stage)
                .companyName(company)
                .positionTitle(position)
                .build();
    }

    // ========================================================================
    // 1. 各状态分支的文案
    // ========================================================================

    @Nested
    @DisplayName("状态文案")
    class StatusCopy {

        @Test
        @DisplayName("APPLIED → 投递成功")
        void applied() {
            List<String> result = NotificationMessageBuilder.build(
                    event("APPLIED", "APPLIED", "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "投递成功",
                    "字节跳动 · 后端工程师 已投递，祝好运！");
        }

        @Test
        @DisplayName("ONLINE_ASSESSMENT → 进展更新")
        void onlineAssessment() {
            List<String> result = NotificationMessageBuilder.build(
                    event("ONLINE_ASSESSMENT", "EXAM", "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "进展更新",
                    "字节跳动 · 后端工程师 进入测评/笔试阶段");
        }

        @Test
        @DisplayName("INTERVIEW → 面试安排（带当前阶段）")
        void interview() {
            List<String> result = NotificationMessageBuilder.build(
                    event("INTERVIEW", "INTERVIEW_2", "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "面试安排",
                    "字节跳动 · 后端工程师 面试进展已更新（INTERVIEW_2）");
        }

        @Test
        @DisplayName("HR_INTERVIEW → HR 面试")
        void hrInterview() {
            List<String> result = NotificationMessageBuilder.build(
                    event("HR_INTERVIEW", "HR_INTERVIEW", "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "HR 面试",
                    "字节跳动 · 后端工程师 进入 HR 面试环节");
        }

        @Test
        @DisplayName("OFFER → 拿到 Offer 🎉")
        void offer() {
            List<String> result = NotificationMessageBuilder.build(
                    event("OFFER", "OFFER", "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "拿到 Offer 🎉",
                    "恭喜！字节跳动 · 后端工程师 向你发出了 Offer");
        }

        @Test
        @DisplayName("REJECTED → 投递未通过")
        void rejected() {
            List<String> result = NotificationMessageBuilder.build(
                    event("REJECTED", "REJECTED", "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "投递未通过",
                    "字节跳动 · 后端工程师 未通过，别灰心，继续加油");
        }

        @Test
        @DisplayName("WITHDRAWN → 投递已撤回")
        void withdrawn() {
            List<String> result = NotificationMessageBuilder.build(
                    event("WITHDRAWN", "WITHDRAWN", "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "投递已撤回",
                    "字节跳动 · 后端工程师 投递已撤回");
        }

        @Test
        @DisplayName("未知状态 → 兜底文案")
        void unknownStatus() {
            List<String> result = NotificationMessageBuilder.build(
                    event("FOO", null, "字节跳动", "后端工程师"));

            assertThat(result).containsExactly(
                    "投递进展",
                    "字节跳动 · 后端工程师 状态变更为 FOO");
        }
    }

    // ========================================================================
    // 2. 公司 / 岗位缺失的兜底
    // ========================================================================

    @Nested
    @DisplayName("公司/岗位缺失兜底")
    class Fallback {

        @Test
        @DisplayName("公司岗位都为空 → 用投递 ID 兜底")
        void bothMissingUsesApplicationId() {
            List<String> result = NotificationMessageBuilder.build(
                    event("APPLIED", "APPLIED", null, null));

            assertThat(result.get(1)).isEqualTo("投递 #1 已投递，祝好运！");
        }

        @Test
        @DisplayName("只有公司 → 不带分隔符")
        void onlyCompany() {
            List<String> result = NotificationMessageBuilder.build(
                    event("APPLIED", "APPLIED", "字节跳动", null));

            assertThat(result.get(1)).isEqualTo("字节跳动 已投递，祝好运！");
        }

        @Test
        @DisplayName("只有岗位 → 不带分隔符")
        void onlyPosition() {
            List<String> result = NotificationMessageBuilder.build(
                    event("APPLIED", "APPLIED", null, "后端工程师"));

            assertThat(result.get(1)).isEqualTo("后端工程师 已投递，祝好运！");
        }
    }
}
