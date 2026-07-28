-- ============================================================
-- OfferPilot — offer_auth 数据库初始化
-- 认证服务：用户登录凭证
-- ============================================================

CREATE DATABASE IF NOT EXISTS offer_auth
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE offer_auth;

-- 用户登录凭证表（与用户信息分离，认证服务专用）
CREATE TABLE IF NOT EXISTS user_account
(
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id      BIGINT       NOT NULL COMMENT '关联 offer_user.user',
    username     VARCHAR(50)  NOT NULL COMMENT '用户名',
    password     VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1:启用 0:禁用',
    last_login_at DATETIME    NULL     COMMENT '最后登录时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户登录凭证';
