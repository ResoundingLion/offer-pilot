package com.offerpilot.notification.service;

import com.offerpilot.notification.entity.Notification;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 创建一条通知（供 MQ 消费者调用）
     */
    void create(Long userId, String type, String title, String content);

    /**
     * 查询用户全部通知（按创建时间倒序）
     */
    List<Notification> listByUserId(Long userId);

    /**
     * 查询未读通知数（顶栏红点）
     */
    long countUnread(Long userId);

    /**
     * 单条通知标记已读（校验归属）
     */
    void markRead(Long id, Long userId);

    /**
     * 全部通知标记已读
     */
    void markAllRead(Long userId);
}
