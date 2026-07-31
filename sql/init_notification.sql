-- ============================================================
-- OfferPilot — offer_notification 数据库初始化
-- 通知服务：站内信
-- ============================================================

CREATE DATABASE IF NOT EXISTS offer_notification
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE offer_notification;

-- ============================================================
-- 站内通知表
-- 由投递状态变更事件（RabbitMQ）驱动生成，用户在顶栏铃铛查看
-- ============================================================
CREATE TABLE IF NOT EXISTS notification (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT        NOT NULL COMMENT '所属用户',
    type       VARCHAR(30)   NOT NULL COMMENT '通知类型：STATUS_CHANGE',
    title      VARCHAR(100)  NOT NULL COMMENT '标题，如「投递成功」',
    content    VARCHAR(500)  NOT NULL COMMENT '内容文案',
    is_read    TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '0 未读 / 1 已读',
    created_at DATETIME      NOT NULL,
    updated_at DATETIME      NOT NULL,
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_user_created (user_id, created_at)
) COMMENT '站内通知';
