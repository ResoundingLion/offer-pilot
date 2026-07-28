-- ============================================================
-- OfferPilot — offer_application 数据库初始化
-- 投递服务：投递记录、面试、Offer
-- ============================================================

CREATE DATABASE IF NOT EXISTS offer_application
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE offer_application;

-- 投递记录表
CREATE TABLE IF NOT EXISTS application
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '投递者',
    company_id  BIGINT       NOT NULL COMMENT '公司ID（冗余）',
    position_id BIGINT       NOT NULL COMMENT '岗位ID',
    status      VARCHAR(20)  NOT NULL COMMENT '投递状态',
    source      VARCHAR(20)  NULL     COMMENT '投递渠道',
    applied_at  DATETIME     NULL     COMMENT '投递日期',
    notes       TEXT         NULL     COMMENT '备注',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='投递记录';

-- 面试记录表
CREATE TABLE IF NOT EXISTS interview
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    application_id  BIGINT       NOT NULL COMMENT '关联投递',
    round           VARCHAR(20)  NOT NULL COMMENT '面试轮次',
    scheduled_at    DATETIME     NOT NULL COMMENT '面试时间',
    interview_type  VARCHAR(20)  NULL     COMMENT '线上面/线下面',
    location        VARCHAR(100) NULL     COMMENT '面试地点/链接',
    interviewer     VARCHAR(50)  NULL     COMMENT '面试官',
    result          VARCHAR(20)  NULL     COMMENT '待定/通过/未通过',
    feedback        TEXT         NULL     COMMENT '面试反馈',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_application_id (application_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='面试记录';

-- Offer 表
CREATE TABLE IF NOT EXISTS offer
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    application_id  BIGINT       NOT NULL COMMENT '关联投递',
    salary          VARCHAR(100) NULL     COMMENT '薪资描述',
    bonus           VARCHAR(100) NULL     COMMENT '奖金/期权',
    stock           VARCHAR(100) NULL     COMMENT '股票',
    benefits        TEXT         NULL     COMMENT '福利',
    deadline        DATE         NULL     COMMENT 'Offer 有效期',
    status          VARCHAR(20)  NOT NULL COMMENT '待接受/已接受/已拒绝',
    remark          TEXT         NULL     COMMENT '备注',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_application_id (application_id),
    INDEX idx_application_id (application_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='Offer 信息';
