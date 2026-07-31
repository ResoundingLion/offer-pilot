package com.offerpilot.notification.mq;

import com.offerpilot.api.event.ApplicationEvent;
import com.offerpilot.notification.service.MailService;
import com.offerpilot.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.offerpilot.api.mq.MqConstants.STATUS_QUEUE;

/**
 * 投递状态变更事件消费者（offer-notification 侧）
 * <p>
 * 监听状态变更队列 → 生成站内信文案 → 落库 → 发送邮件。
 * <p>
 * 与 offer-application 内的 StatusChangeConsumer 不同：
 * 这里是通知服务的真正闭环（生成通知并持久化），
 * 那边保留只做日志记录。两者消费同一队列，互不干扰。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final MailService mailService;

    @RabbitListener(queues = STATUS_QUEUE)
    public void handleStatusChange(ApplicationEvent event) {
        // 防御：空消息 / 无用户信息直接忽略
        if (event == null || event.getUserId() == null) {
            log.warn("收到空事件，忽略");
            return;
        }

        List<String> message = NotificationMessageBuilder.build(event);
        String title = message.get(0);
        String content = message.get(1);

        log.info("📨 [通知] 用户#{}，{}（{}）：{}", event.getUserId(), title, event.getNewStatus(), content);

        // 1. 落库（站内信）
        notificationService.create(event.getUserId(), NotificationMessageBuilder.TYPE_STATUS_CHANGE, title, content);

        // 2. 发送邮件（当前为日志模拟）
        mailService.send(event.getUserId(), title, content);
    }
}
