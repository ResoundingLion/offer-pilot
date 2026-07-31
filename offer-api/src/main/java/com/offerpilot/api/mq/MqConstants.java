package com.offerpilot.api.mq;

/**
 * RabbitMQ 常量定义
 * <p>
 * 交换机 / 队列 / 路由键由 offer-application（生产者）与 offer-notification（消费者）共享。
 * 放在公共模块 offer-api 中，避免各服务各自维护魔法字符串导致不一致。
 */
public final class MqConstants {

    private MqConstants() {
    }

    /** Topic 交换机名称 */
    public static final String EXCHANGE = "offer.application.exchange";

    /** 状态变更队列名称 */
    public static final String STATUS_QUEUE = "offer.application.status.queue";

    /** 状态变更路由键 */
    public static final String STATUS_ROUTING_KEY = "application.status.change";
}
