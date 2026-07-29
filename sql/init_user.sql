-- ============================================================
-- OfferPilot — offer_user 数据库初始化
-- 用户服务：用户信息、公司、岗位
-- ============================================================

CREATE DATABASE IF NOT EXISTS offer_user
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE offer_user;

-- 用户信息表
CREATE TABLE IF NOT EXISTS `user`
(
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    email      VARCHAR(100) NULL     COMMENT '邮箱',
    phone      VARCHAR(20)  NULL     COMMENT '手机号',
    avatar     VARCHAR(255) NULL     COMMENT '头像 URL',
    name       VARCHAR(50)  NOT NULL COMMENT '姓名',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_email (email),
    UNIQUE KEY uk_phone (phone)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='用户信息';

-- 公司表
CREATE TABLE IF NOT EXISTS company
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    name        VARCHAR(100) NOT NULL COMMENT '公司名称',
    industry    VARCHAR(50)  NULL     COMMENT '所属行业',
    website     VARCHAR(255) NULL     COMMENT '公司官网',
    location    VARCHAR(100) NULL     COMMENT '所在地',
    size        VARCHAR(20)  NULL     COMMENT '规模描述',
    description TEXT         NULL     COMMENT '公司简介',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='公司信息';

-- 岗位表
CREATE TABLE IF NOT EXISTS position
(
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    company_id      BIGINT       NOT NULL COMMENT '所属公司',
    title           VARCHAR(100) NOT NULL COMMENT '岗位名称',
    salary_min      INT          NULL     COMMENT '最低薪资(K/月)',
    salary_max      INT          NULL     COMMENT '最高薪资(K/月)',
    city            VARCHAR(50)  NULL     COMMENT '工作城市',
    education       VARCHAR(20)  NULL     COMMENT '学历要求',
    experience      VARCHAR(20)  NULL     COMMENT '经验要求',
    employment_type VARCHAR(20)  NULL     COMMENT '全职/实习/兼职',
    description     TEXT         NULL     COMMENT '岗位描述',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '0:关闭 1:招聘中',
    deadline        DATE         NULL     COMMENT '截止日期',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_company_id (company_id),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='岗位信息';

-- 简历管理表（支持多版本）
CREATE TABLE IF NOT EXISTS resume
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL COMMENT '所属用户',
    title       VARCHAR(100) NOT NULL COMMENT '简历标题，同标题=同简历不同版本',
    version     INT          NOT NULL DEFAULT 1 COMMENT '版本号，同标题下自增',
    content     TEXT         NULL     COMMENT '简历内容（结构化JSON，预留AI分析）',
    file_url    VARCHAR(500) NULL     COMMENT '上传的简历文件URL（MinIO存储）',
    summary     TEXT         NULL     COMMENT '简历摘要（AI生成，预留）',
    is_current  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否当前使用版本',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_user_title_version (user_id, title, version)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='简历管理';
