package com.offerpilot.notification.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内通知
 * <p>
 * 由投递状态变更事件（RabbitMQ）驱动生成，用户在顶栏铃铛中查看。
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户
     */
    private Long userId;

    /**
     * 通知类型（当前仅 STATUS_CHANGE，后续可扩展 INTERVIEW_REMIND 等）
     */
    private String type;

    /**
     * 标题（如「投递成功」「拿到 Offer 🎉」）
     */
    private String title;

    /**
     * 内容文案（如「字节跳动 · 后端工程师 已投递，祝好运！」）
     */
    private String content;

    /**
     * 是否已读：0 未读 / 1 已读
     */
    private Boolean isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
