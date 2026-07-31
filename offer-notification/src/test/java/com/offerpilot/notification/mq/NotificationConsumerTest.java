package com.offerpilot.notification.mq;

import com.offerpilot.api.event.ApplicationEvent;
import com.offerpilot.notification.service.MailService;
import com.offerpilot.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * NotificationConsumer 单元测试
 * <p>
 * 覆盖：正常事件 → 落库 + 发邮件；空事件 / 无用户 → 忽略。
 */
@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private NotificationConsumer consumer;

    private ApplicationEvent validEvent() {
        return ApplicationEvent.builder()
                .applicationId(1L)
                .userId(3L)
                .newStatus("APPLIED")
                .companyName("字节跳动")
                .positionTitle("后端工程师")
                .build();
    }

    // ========================================================================
    // 1. 正常事件
    // ========================================================================

    @Nested
    @DisplayName("正常事件")
    class ValidEvent {

        @Test
        @DisplayName("落库一条站内信 + 发送一封邮件")
        void createsNotificationAndSendsMail() {
            consumer.handleStatusChange(validEvent());

            verify(notificationService).create(
                    3L, "STATUS_CHANGE", "投递成功", "字节跳动 · 后端工程师 已投递，祝好运！");
            verify(mailService).send(3L, "投递成功", "字节跳动 · 后端工程师 已投递，祝好运！");
        }
    }

    // ========================================================================
    // 2. 防御分支
    // ========================================================================

    @Nested
    @DisplayName("防御分支")
    class GuardClauses {

        @Test
        @DisplayName("event 为 null 时直接忽略")
        void ignoresNullEvent() {
            consumer.handleStatusChange(null);

            verify(notificationService, never()).create(org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyString());
            verify(mailService, never()).send(org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyString());
        }

        @Test
        @DisplayName("userId 为 null 时直接忽略")
        void ignoresEventWithoutUser() {
            ApplicationEvent event = validEvent();
            event.setUserId(null);

            consumer.handleStatusChange(event);

            verify(notificationService, never()).create(org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyString());
            verify(mailService, never()).send(org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.anyString(),
                    org.mockito.ArgumentMatchers.anyString());
        }
    }
}
