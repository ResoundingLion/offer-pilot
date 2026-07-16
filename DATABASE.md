# OfferPilot 数据库设计

## 设计原则

- 每个微服务拥有独立数据库
- 跨库不建外键，通过业务代码维护一致性
- 所有表使用 `InnoDB` 引擎、`utf8mb4` 字符集
- 主键统一使用 `BIGINT` 自增 ID
- 统一时间字段：`created_at` / `updated_at`（由 MyBatis-Plus 自动填充）

## 数据库划分

| 服务 | 数据库 | 包含表 |
|------|--------|--------|
| offer-auth | offer_auth | user_account |
| offer-user | offer_user | user, company, position |
| offer-application | offer_application | application, interview, offer |

## 表结构

### offer_auth.user_account

用户登录凭证，与用户信息分离（认证服务专用）。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | UNIQUE, NOT NULL | 关联 offer_user.user |
| username | VARCHAR(50) | UNIQUE, NOT NULL | |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密 |
| status | TINYINT | DEFAULT 1 | 1:启用 0:禁用 |
| last_login_at | DATETIME | | 最后登录时间 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

索引：`uk_username(username)`, `idx_user_id(user_id)`

### offer_user.user

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| email | VARCHAR(100) | UNIQUE | |
| phone | VARCHAR(20) | UNIQUE | |
| avatar | VARCHAR(255) | | 头像 URL |
| name | VARCHAR(50) | NOT NULL | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

### offer_user.company

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | NOT NULL, INDEX | 所属用户 |
| name | VARCHAR(100) | NOT NULL | |
| industry | VARCHAR(50) | | |
| website | VARCHAR(255) | | |
| location | VARCHAR(100) | | |
| size | VARCHAR(20) | | 规模描述 |
| description | TEXT | | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

索引：`idx_user_id(user_id)`

### offer_user.position

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| company_id | BIGINT | NOT NULL, INDEX | 所属公司 |
| title | VARCHAR(100) | NOT NULL | 岗位名称 |
| salary_min | INT | | 最低薪资(K/月) |
| salary_max | INT | | 最高薪资(K/月) |
| city | VARCHAR(50) | | |
| education | VARCHAR(20) | | 学历要求 |
| experience | VARCHAR(20) | | 经验要求 |
| employment_type | VARCHAR(20) | | 全职/实习/兼职 |
| description | TEXT | | |
| status | TINYINT | DEFAULT 1 | 0:关闭 1:招聘中 |
| deadline | DATE | | 截止日期 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

索引：`idx_company_id(company_id)`

### offer_application.application

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | NOT NULL, INDEX | 投递者 |
| company_id | BIGINT | NOT NULL | 冗余字段，便于统计 |
| position_id | BIGINT | NOT NULL | |
| status | VARCHAR(20) | NOT NULL | 见状态流转 |
| source | VARCHAR(20) | | 投递渠道 |
| applied_at | DATETIME | | 投递日期 |
| notes | TEXT | | 备注 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

索引：`idx_user_id(user_id)`, `idx_status(status)`, `idx_company_id(company_id)`

### offer_application.interview

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| application_id | BIGINT | NOT NULL, INDEX | 关联投递 |
| round | VARCHAR(20) | NOT NULL | 面试轮次 |
| scheduled_at | DATETIME | NOT NULL | 面试时间 |
| interview_type | VARCHAR(20) | | 线上面/线下面 |
| location | VARCHAR(100) | | |
| interviewer | VARCHAR(50) | | |
| result | VARCHAR(20) | | 待定/通过/未通过 |
| feedback | TEXT | | 面试反馈 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

索引：`idx_application_id(application_id)`

### offer_application.offer

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| application_id | BIGINT | NOT NULL, UNIQUE, INDEX | 关联投递 |
| salary | VARCHAR(100) | | 薪资描述 |
| bonus | VARCHAR(100) | | 奖金/期权 |
| stock | VARCHAR(100) | | 股票 |
| benefits | TEXT | | 福利 |
| deadline | DATE | | Offer 有效期 |
| status | VARCHAR(20) | NOT NULL | 待接受/已接受/已拒绝 |
| remark | TEXT | | 备注 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

索引：`idx_application_id(application_id)`

## 状态流转（核心逻辑）

```
SAVED ──→ APPLIED ──→ ONLINE_ASSESSMENT ──→ INTERVIEW ──→ HR_INTERVIEW ──→ OFFER
                                                                              │
                                    REJECTED ←───────────────────────────────┘
                                    WITHDRAWN (任意状态均可撤回)
```

- **SAVED：** 收藏岗位，未投递
- **APPLIED：** 已投递
- **ONLINE_ASSESSMENT：** 笔试/测评阶段
- **INTERVIEW：** 技术面试阶段
- **HR_INTERVIEW：** HR 面试阶段
- **OFFER：** 已发 Offer
- **REJECTED：** 被拒（从任何中间状态均可进入）
- **WITHDRAWN：** 主动撤回（从任何状态均可进入）

## 跨库查询策略

投递记录需要展示公司名和岗位名时，两种方案：
1. **优先展示缓存数据：** 创建投递时冗余写入公司/岗位名称到 application 表
2. **需完整信息时：** 通过 OpenFeign 调用 offer-user 接口查询

> MVP 采用方案 1，减少跨服务调用，性能好。后续如果数据一致性要求更高再切方案 2。

### 要点说明

数据库按服务拆分隔离，不建外键。company_id 和 position_id 在 application 表冗余存储是为了方便统计时不用跨库 JOIN——这是故意的反范式设计，不是低级错误。
