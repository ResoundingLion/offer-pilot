package com.offerpilot.application.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.offerpilot.application.config.RabbitMQConfig.STATUS_QUEUE;

/**
 * 投递状态变更事件消费者
 * <p>
 * 监听 {@link RabbitMQConfig#STATUS_QUEUE}，异步处理状态变更。
 * 当前行为：打印日志。
 * 后续可扩展：
 *   - 发送站内信 / 邮件通知
 *   - 记录操作审计日志
 *   - 触发 AI 智能分析
 */
@Slf4j
@Component
public class StatusChangeConsumer {

    @RabbitListener(queues = STATUS_QUEUE)
    public void handleStatusChange(ApplicationEvent event) {
        log.info("📨 [状态变更] applicationId={}, {} → {}, 阶段={}, 用户={}, 时间={}",
                event.getApplicationId(),
                event.getOldStatus(),
                event.getNewStatus(),
                event.getCurrentStage(),
                event.getUserId(),
                event.getTimestamp());
    }
}
