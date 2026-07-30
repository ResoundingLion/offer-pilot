# OfferPilot 数据库设计

## 设计原则

- 每个微服务拥有独立数据库
- 跨库不建外键，通过业务代码维护一致性
- 所有表使用 `InnoDB` 引擎、`utf8mb4` 字符集
- 主键统一使用 `BIGINT` 自增 ID
- 统一时间字段：`created_at` / `updated_at`（由 MyBatis-Plus 自动填充）

---

## ER 关系图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              offer_auth                                     │
│  ┌──────────────────────────────────────┐                                   │
│  │          user_account                │                                   │
│  │──────────────────────────────────────│                                   │
│  │ PK  id            BIGINT            │────── 1:1 ──────┐                 │
│  │     user_id       BIGINT  (UNIQUE)  │                 │                 │
│  │     username      VARCHAR(50)       │                 │                 │
│  │     password      VARCHAR(255)      │                 │                 │
│  │     status        TINYINT           │                 │                 │
│  │     last_login_at DATETIME          │                 ▼                 │
│  └──────────────────────────────────────┘          offer_user              │
├─────────────────────────────────────────────────────────────────────────────┤
│                              offer_user                                     │
│  ┌──────────────────────────────────────┐  ┌──────────────────────────────┐ │
│  │             user                     │  │          company            │ │
│  │──────────────────────────────────────│  │──────────────────────────────│ │
│  │ PK  id            BIGINT            │◀─┤ PK  id        BIGINT        │ │
│  │     email         VARCHAR(100)      │  │     user_id   BIGINT        │ │
│  │     phone         VARCHAR(20)       │  │     name      VARCHAR(100)  │ │
│  │     avatar        VARCHAR(255)      │  │     industry  VARCHAR(50)   │ │
│  │     name          VARCHAR(50)       │  │     website   VARCHAR(255)  │ │
│  └──────────────────────────────────────┘  │     location  VARCHAR(100)  │ │
│                                            │     size      VARCHAR(20)   │ │
│                                            │     description TEXT        │ │
│                                            └───────────┬──────────────────┘ │
│                                                        │ 1:N               │
│                                                        ▼                   │
│                                            ┌──────────────────────────────┐ │
│                                            │          position            │ │
│                                            │──────────────────────────────│ │
│                                            │ PK  id        BIGINT        │ │
│                                            │     company_id BIGINT       │ │
│                                            │     title     VARCHAR(100)  │ │
│                                            │     salary_min INT          │ │
│                                            │     salary_max INT          │ │
│                                            │     city      VARCHAR(50)   │ │
│                                            └──────────────────────────────┘ │
│                                                                             │
│  ┌──────────────────────────────────────┐                                   │
│  │             resume                   │<── user_id 1:N                   │
│  │──────────────────────────────────────│                                   │
│  │ PK  id            BIGINT            │                                   │
│  │     user_id       BIGINT            │                                   │
│  │     title         VARCHAR(100)      │                                   │
│  │     version       INT               │                                   │
│  │     content       TEXT              │                                   │
│  │     file_url      VARCHAR(500)      │                                   │
│  │     summary       TEXT              │                                   │
│  │     content_text  MEDIUMTEXT        │                                   │
│  │     is_current    TINYINT(1)        │                                   │
│  └──────────────────────────────────────┘                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                           offer_ai（2026-07-30 新增）                        │
│  ┌──────────────────────────────────────┐                                   │
│  │          conversation                │                                   │
│  │──────────────────────────────────────│                                   │
│  │ PK  id            BIGINT            │── 1:N ──┐                         │
│  │     user_id       BIGINT            │         │                         │
│  │     title         VARCHAR(200)      │         │                         │
│  └──────────────────────────────────────┘         │                         │
│                                                   │                         │
│  ┌──────────────────────────────────────┐         │                         │
│  │       conversation_message           │         │                         │
│  │──────────────────────────────────────│         │                         │
│  │ PK  id            BIGINT            │◀────────┘                         │
│  │     conversation_id BIGINT          │                                   │
│  │     role          VARCHAR(20)       │                                   │
│  │     content       TEXT              │                                   │
│  │     tool_name     VARCHAR(100)      │                                   │
│  │     tool_args     TEXT              │                                   │
│  │     tool_result   TEXT              │                                   │
│  │     msg_index     INT               │                                   │
│  └──────────────────────────────────────┘                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                           offer_application                                 │
│  ┌──────────────────────────────────────┐                                   │
│  │          application                 │                                   │
│  │──────────────────────────────────────│                                   │
│  │ PK  id            BIGINT            │── 1:N ──┐                         │
│  │     user_id       BIGINT            │         │                         │
│  │     company_id    BIGINT            │         │                         │
│  │     position_id   BIGINT            │         │                         │
│  │     status        VARCHAR(20)       │         │                         │
│  │     source        VARCHAR(20)       │         │                         │
│  │     applied_at    DATETIME          │         │                         │
│  │     notes         TEXT              │         │                         │
│  │     pipeline_config VARCHAR(100)    │         │                         │
│  │     current_stage VARCHAR(50)       │         │                         │
│  └──────────────────────────────────────┘         │                         │
│                                                   │                         │
│  ┌──────────────────────────────────────┐         │                         │
│  │           interview                  │         │                         │
│  │──────────────────────────────────────│         │                         │
│  │ PK  id            BIGINT            │◀────────┘                         │
│  │     application_id BIGINT           │                                   │
│  │     round         VARCHAR(20)       │                                   │
│  │     scheduled_at  DATETIME          │                                   │
│  │     interview_type VARCHAR(20)      │                                   │
│  │     location      VARCHAR(100)      │                                   │
│  │     interviewer   VARCHAR(50)       │                                   │
│  │     result        VARCHAR(20)       │                                   │
│  │     feedback      TEXT              │                                   │
│  └──────────────────────────────────────┘                                   │
│                                                                             │
│  ┌──────────────────────────────────────┐                                   │
│  │             offer                    │                                   │
│  │──────────────────────────────────────│                                   │
│  │ PK  id            BIGINT            │◀────────┘ (0..1)                  │
│  │     application_id BIGINT  (UNIQUE) │                                   │
│  │     salary        VARCHAR(100)      │                                   │
│  │     bonus         VARCHAR(100)      │                                   │
│  │     stock         VARCHAR(100)      │                                   │
│  │     benefits      TEXT              │                                   │
│  │     deadline      DATE              │                                   │
│  │     status        VARCHAR(20)       │                                   │
│  │     remark        TEXT              │                                   │
│  └──────────────────────────────────────┘                                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 关系说明

| 关系 | 类型 | 说明 |
|------|:----:|------|
| user_account → user | 1:1 | `user_account.user_id` = `user.id` |
| user → company | 1:N | 一个用户可以添加多家公司 |
| company → position | 1:N | 一家公司可以有多个岗位 |
| user → application | 1:N | 一个用户可以有多次投递 |
| user → resume | 1:N | 一个用户可以有多个简历版本 |
| **user → conversation** | **1:N** | **一个用户可多次对话（2026-07-30）** |
| company → application | 1:N | 冗余关系，便于统计 |
| position → application | 1:N | 一个岗位可以被多人投递 |
| application → interview | 1:N | 一次投递可以有多次面试 |
| application → offer | 1:0..1 | 一次投递最多只有一个 Offer（`UNIQUE`） |
| **conversation → conversation_message** | **1:N** | **一次对话有多条消息** |

---

## 数据库划分

| 服务 | 数据库 | 包含表 |
|------|--------|--------|
| offer-auth | offer_auth | user_account |
| offer-user | offer_user | user, company, position, resume |
| **offer-ai** | **offer_ai** | **conversation, conversation_message** |
| offer-application | offer_application | application, interview, offer |

---

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

**索引：** `uk_username(username)`、`idx_user_id(user_id)`、`uk_user_id(user_id)`

---

### offer_user.user

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| email | VARCHAR(100) | UNIQUE | |
| phone | VARCHAR(20) | UNIQUE | |
| avatar | VARCHAR(255) | | 头像 URL（MinIO 预签名 URL） |
| name | VARCHAR(50) | NOT NULL | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**索引：** `uk_email(email)`、`uk_phone(phone)`

---

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

**索引：** `idx_user_id(user_id)`

---

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

**索引：** `idx_company_id(company_id)`、`idx_title_status(title, status)`（复合查询）

---

### offer_user.resume

简历管理表，支持多版本（同一 `title` 下 `version` 递增）。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | NOT NULL, INDEX | 所属用户 |
| title | VARCHAR(100) | NOT NULL | 简历标题，同标题 = 同一份简历的不同版本 |
| version | INT | NOT NULL, DEFAULT 1 | 版本号，同一 title 下自增 |
| content | TEXT | | 简历内容（结构化 JSON，预留 AI 分析） |
| file_url | VARCHAR(500) | | 上传的简历文件 URL（MinIO 存储） |
| summary | TEXT | | 简历摘要（AI 生成，预留） |
| content_text | MEDIUMTEXT | | PDF 提取的纯文本（上限 16MB，供 AI 分析） |
| is_current | TINYINT(1) | NOT NULL, DEFAULT 0 | 是否当前使用版本（每用户每 title 仅一条为 1） |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**索引：** `idx_user_id(user_id)`、`uk_user_title_version(user_id, title, version)`（联合唯一，防止重复版本）

> 为什么不直接用 `group_id` 分组？用 `title` 分组更直观——用户可以一眼看出"Java后端简历 v3"是什么。如果用 group_id，前端需要额外查标题映射表，增加复杂度。

---

### offer_ai.conversation

AI Agent 对话会话表。每次用户与 Agent 的完整交互为一个对话，包含多条消息。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| user_id | BIGINT | NOT NULL, INDEX | 所属用户 |
| title | VARCHAR(200) | | 对话标题（取首条消息前 30 字） |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**索引：** `idx_user_id(user_id)`

---

### offer_ai.conversation_message

对话消息，支持 USER / ASSISTANT / TOOL_USE / TOOL_RESULT 四种角色。
TOOL_USE 和 TOOL_RESULT 存 LLM 工具调用的参数和结果。

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| conversation_id | BIGINT | NOT NULL, INDEX, FK | 所属对话 |
| role | VARCHAR(20) | NOT NULL | USER / ASSISTANT / TOOL_USE / TOOL_RESULT |
| content | TEXT | | 文本内容或 tool_use_id |
| tool_name | VARCHAR(100) | | LLM 调用的工具名 |
| tool_args | TEXT | | 工具参数 JSON |
| tool_result | TEXT | | 工具执行结果 JSON |
| msg_index | INT | NOT NULL | 消息序号 |
| created_at | DATETIME | NOT NULL | |

**索引：** `idx_conversation_id(conversation_id)`，外键 `fk_message_conversation` REFERENCES `conversation(id)`

---

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
| pipeline_config | VARCHAR(100) | | 可选阶段配置: ASSESSMENT,EXAM,... |
| current_stage | VARCHAR(50) | | 当前流水线阶段 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**索引：** `idx_user_id(user_id)`、`idx_status(status)`、`idx_company_id(company_id)`、`idx_user_status(user_id, status)`（用户按状态筛选）

---

### offer_application.interview

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| application_id | BIGINT | NOT NULL, INDEX | 关联投递 |
| round | VARCHAR(20) | NOT NULL | 面试轮次（FIRST/SECOND/THIRD/HR） |
| scheduled_at | DATETIME | NOT NULL | 面试时间 |
| interview_type | VARCHAR(20) | | ONLINE / OFFLINE |
| location | VARCHAR(100) | | 面试地点/链接 |
| interviewer | VARCHAR(50) | | |
| result | VARCHAR(20) | | PENDING / PASSED / FAILED |
| feedback | TEXT | | 面试反馈 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**索引：** `idx_application_id(application_id)`、`idx_app_round(application_id, round)`（唯一轮次）

---

### offer_application.offer

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK | |
| application_id | BIGINT | NOT NULL, UNIQUE, INDEX | 关联投递（一个投递最多一个 Offer） |
| salary | VARCHAR(100) | | 薪资描述（如 "25k × 15薪"） |
| bonus | VARCHAR(100) | | 奖金/签字费 |
| stock | VARCHAR(100) | | 股票/RSU |
| benefits | TEXT | | 福利 |
| deadline | DATE | | Offer 有效期 |
| status | VARCHAR(20) | NOT NULL | PENDING / ACCEPTED / DECLINED |
| remark | TEXT | | 备注 |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | NOT NULL | |

**索引：** `idx_application_id(application_id)`（同时也是 UNIQUE）

---

## 状态流转（核心逻辑）

### Application 状态机

```
SAVED ──→ APPLIED ──→ ONLINE_ASSESSMENT ──→ INTERVIEW ──→ HR_INTERVIEW ──→ OFFER ──→ ACCEPTED
                                                                                        ↓
                                    REJECTED ←───────────────────────────────────── DECLINED
                                    WITHDRAWN (任意状态均可撤回)
```

| 状态 | 含义 | 是否终止态 |
|------|------|:----------:|
| SAVED | 收藏岗位，未投递 | 否 |
| APPLIED | 已投递 | 否 |
| ONLINE_ASSESSMENT | 笔试/测评阶段 | 否 |
| INTERVIEW | 技术面试阶段 | 否 |
| HR_INTERVIEW | HR 面试阶段 | 否 |
| OFFER | 已发 Offer | 否 |
| ACCEPTED | 已接受 Offer | **是** |
| DECLINED | 已拒绝 Offer | **是** |
| REJECTED | 被拒 | **是** |
| WITHDRAWN | 主动撤回 | **是** |

> **关键规则：** 状态只能沿正向流动（SAVED → ... → OFFER），不能回退。终止态（ACCEPTED/DECLINED/REJECTED/WITHDRAWN）不可再变更。REJECTED 和 WITHDRAWN 可从任意非终止态进入。

### Offer 状态机

```
PENDING ──→ ACCEPTED
         └─→ DECLINED
```

| 状态 | 含义 | 是否终止态 |
|------|------|:----------:|
| PENDING | 待处理 | 否 |
| ACCEPTED | 已接受 | **是** |
| DECLINED | 已拒绝 | **是** |

> ACCEPTED/DECLINED 不可逆转回 PENDING。

---

## 跨库查询策略

投递记录需要展示公司名和岗位名时：

```
ApplicationVO.companyName    ← Feign 调用 offer-user 内部接口
ApplicationVO.positionTitle  ← Feign 调用 offer-user 内部接口
                              ↓
                          Redis 缓存（Cache-Aside 模式）
                          过期时间 1h + 随机偏移防雪崩
                          空值缓存防穿透
```

> company_id 和 position_id 在 application 表冗余存储是为了方便统计时不用跨库 JOIN——这是故意的反范式设计，不是低级错误。
