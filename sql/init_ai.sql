-- ============================================================
-- OfferPilot — offer_ai 数据库初始化
-- AI 服务：对话历史
-- ============================================================

CREATE DATABASE IF NOT EXISTS offer_ai
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE offer_ai;

-- ============================================================
-- 对话会话表
-- ============================================================
CREATE TABLE IF NOT EXISTS conversation (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    title       VARCHAR(200) NULL     COMMENT '对话标题（取首条消息的前 N 字）',
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME     NOT NULL,
    INDEX idx_user (user_id)
) COMMENT '对话会话';

-- ============================================================
-- 对话消息表
-- role: USER / ASSISTANT / TOOL_USE / TOOL_RESULT
-- tool_name / tool_args / tool_result 只在 TOOL_USE 和 TOOL_RESULT 时有值
-- ============================================================
CREATE TABLE IF NOT EXISTS conversation_message (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT       NOT NULL,
    role            VARCHAR(20)  NOT NULL COMMENT 'USER / ASSISTANT / TOOL_USE / TOOL_RESULT',
    content         TEXT         NULL     COMMENT '文本内容',
    tool_name       VARCHAR(100) NULL     COMMENT 'LLM 调用的工具名',
    tool_args       TEXT         NULL     COMMENT '工具参数（JSON）',
    tool_result     TEXT         NULL     COMMENT '工具执行结果（JSON）',
    msg_index       INT          NOT NULL COMMENT '消息序号，用于排序',
    created_at      DATETIME     NOT NULL,
    INDEX idx_conversation (conversation_id),
    FOREIGN KEY (conversation_id) REFERENCES conversation(id)
) COMMENT '对话消息';
