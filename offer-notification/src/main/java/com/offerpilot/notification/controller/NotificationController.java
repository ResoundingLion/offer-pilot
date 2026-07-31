package com.offerpilot.notification.controller;

import com.offerpilot.common.result.Result;
import com.offerpilot.notification.entity.Notification;
import com.offerpilot.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 通知服务控制器
 * <p>
 * 所有接口都经过网关 JWT 鉴权，userId 由网关从 Token 解析后
 * 注入 X-User-Id 请求头（见 JwtAuthGlobalFilter）。
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications —— 当前用户全部通知（按时间倒序）
     */
    @GetMapping
    public Result<List<Notification>> list(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(notificationService.listByUserId(userId));
    }

    /**
     * GET /api/notifications/unread-count —— 未读通知数（顶栏红点）
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(Map.of("unreadCount", notificationService.countUnread(userId)));
    }

    /**
     * PUT /api/notifications/{id}/read —— 单条标记已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        notificationService.markRead(id, userId);
        return Result.success();
    }

    /**
     * PUT /api/notifications/read-all —— 全部标记已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllRead(userId);
        return Result.success();
    }

    /**
     * GET /api/notifications/test —— 存活检测（网关白名单，无需 Token）
     */
    @GetMapping("/test")
    public String test() {
        return "offer-notification 服务启动成功！";
    }
}
