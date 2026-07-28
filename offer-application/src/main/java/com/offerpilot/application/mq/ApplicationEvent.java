package com.offerpilot.application.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 投递状态变更事件（MQ 消息体）
 * <p>
 * 当用户一键推进或变更投递状态时，由 advance() 发送到 RabbitMQ。
 * 消费者可据此做日志记录、通知发送等异步处理。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEvent {

    /** 投递记录 ID */
    private Long applicationId;

    /** 变更前状态 */
    private String oldStatus;

    /** 变更后状态 */
    private String newStatus;

    /** 当前流水线阶段（如 INTERVIEW_1 / OFFER） */
    private String currentStage;

    /** 用户 ID */
    private Long userId;

    /** 变更时间（ISO 格式，如 "2026-07-28T10:30:00"） */
    private String timestamp;
}
