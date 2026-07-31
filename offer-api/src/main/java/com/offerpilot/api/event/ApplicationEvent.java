package com.offerpilot.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 投递状态变更事件（RabbitMQ 消息体）
 * <p>
 * 由 offer-application 在投递一键推进时发送，offer-notification 消费后生成站内信 + 邮件通知。
 * <p>
 * 放在公共模块 offer-api 中，保证生产者与消费者反序列化的是同一份契约
 * （跨服务事件共享的标准做法，避免各服务各存一份导致字段漂移）。
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

    /** 公司名称（生产者已通过 Feign 组装，随消息带上，通知服务无需再跨服务查询） */
    private String companyName;

    /** 岗位名称 */
    private String positionTitle;
}
