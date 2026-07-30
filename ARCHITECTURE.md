# OfferPilot 微服务架构设计

## 架构总览

```
Client (浏览器 / Postman)
    │
    ▼
┌──────────────────────────────────────┐
│   Spring Cloud Gateway (8080)        │
│   路由转发 + JWT 鉴权 + 跨域          │
└────┬──────┬──────┬──────┬────────────┘
     │      │      │      │
┌────▼──┐ ┌▼─────┐┌▼─────┐┌▼──────────┐
│ Auth  │ │ User ││ Appl ││ AI        │
│:8081  │ │:8082 ││:8083 ││:8084      │
└───┬───┘ └──┬───┘└──┬──┘└─────┬─────┘
    │        │       │         │
    └────────┴───┬───┴─────────┘
                 │
          ┌──────▼──────┐
          │ Nacos (:8848)│
          └─────────────┘

  ┌──────┬──────┬──────────┬─────────┐
  │ MySQL│ Redis│ RabbitMQ │ MinIO   │
  │:3306 │:6379 │ :5672    │ :9000   │
  └──────┴──────┴──────────┴─────────┘
  基础设施层（Docker Compose 一键部署）
```

**分层说明：**
- **网关层** — 统一入口，负责路由转发、JWT 校验、跨域处理
- **微服务层** — 5 个独立服务，互相通过 Feign 调用，全部注册到 Nacos
- **基础设施层** — Docker Compose 一键启动，与微服务分离

---

## 微服务划分

| 服务 | 端口 | 职责 | 数据库 | 说明 |
|------|:----:|------|--------|------|
| **offer-gateway** | 8080 | 路由转发、统一鉴权、跨域 | 无 | |
| **offer-auth** | 8081 | 登录/注册、JWT 签发与校验 | offer_auth | 独立认证库 |
| **offer-user** | 8082 | 用户管理、公司管理、岗位管理、**简历管理**、文件上传、**PDF 文本提取** | offer_user | 含内部 Feign 接口 + PdfService |
| **offer-application** | 8083 | 投递管理、面试管理、Offer 管理、Pipeline 流水线、Dashboard 统计 | offer_application | 核心业务服务 |
| **offer-ai** | 8084 | **AI Agent（ReAct Tool Calling）+ 对话历史持久化** | **offer_ai** | 通过 Feign 跨服务查数据 |

> **为什么不做成 10 个微服务？** 一个人开发，拆分过细光环境配置就消耗大半时间。公司依赖用户，岗位依赖公司，投递/面试/Offer 属于同一条业务线——5 个服务在当前规模下恰到好处。

---

## 服务间调用

### 同步调用（OpenFeign）

```
offer-application → offer-user    GET /internal/companies/{id}  ← 公司名
offer-application → offer-user    GET /internal/positions/{id}  ← 岗位名

# AI Agent 调用（2026-07-30 新增）
offer-ai → offer-user            GET /internal/resumes/active  ← 当前简历（JD 匹配）
offer-ai → offer-application     GET /internal/applications    ← 投递列表
offer-ai → offer-application     GET /internal/applications/dashboard/stats ← 统计
offer-ai → offer-application     GET /internal/interviews      ← 面试记录
offer-ai → offer-application     GET /internal/offers          ← Offer
```

- 调用结果走 Redis 缓存（Cache-Aside 模式），减少跨服务调用频率
- Feign 配置 Sentinel 熔断降级，失败时返回友好文案或 null（Agent 会处理空结果）

### 异步消息（RabbitMQ）

```
offer-application (advance) → TopicExchange → StatusChangeConsumer
```

- 投递状态变更时发送 `ApplicationEvent` 消息
- try-catch 兜底，MQ 宕机不影响主流程

---

## 注册中心 / 配置中心

| 组件 | 地址 | 用途 |
|------|------|------|
| Nacos Server | `localhost:8848` | 服务注册发现 + 配置中心 |

- 每个服务启动时注册到 Nacos，通过服务名互相发现
- 配置文件统一存储在 Nacos，本地仅保留 `bootstrap.yml`
- 命名规则：`{服务名}-{profile}.yml`

---

## 统一认证方案

```
请求 → Gateway → JwtAuthGlobalFilter 校验 Token
    → 无 Token / Token 过期 → 返回 401
    → Token 有效 → 转发到目标服务（Header 携带 userId）
```

- 使用 JWT（access_token），无状态认证
- 服务间 Feign 调用通过内部接口（`/internal/**`）不携带 Token
- 文件下载路径（`/api/files/**`）在网关放行

---

## 数据存储规划

| 数据库 | 表 | 说明 |
|--------|----|------|
| offer_auth | user_account | 登录凭证，独立认证库 |
| offer_user | user, company, position, resume | 用户信息 + 公司 + 岗位 + 简历 |
| offer_application | application, interview, offer | 投递全流程 |
| **offer_ai** | **conversation, conversation_message** | **AI Agent 对话历史（2026-07-30 新增）** |

- 每个微服务独享数据库，跨库不建外键
- 跨库查询通过 Feign 接口 + 缓存解决，不做跨库 JOIN
- Application 冗余存储 company_id / position_id 是故意的反范式设计，方便统计

---

## 分层架构图（以 offer-application 为例）

```
┌─────────────────────────────────────────────────┐
│                  Controller                      │
│  Application / Interview / Offer / Dashboard     │
└───────────────────────┬─────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────┐
│                   Service                        │
│  CRUD + 状态机 + enrichVO + advance + Pipeline   │
└────┬──────────────┬──────────────┬──────────────┘
     │              │              │
┌────▼──────┐ ┌─────▼──────┐ ┌────▼───────────┐
│  Mapper   │ │   Feign    │ │  MQ / Cache    │
│ MyBatis-  │ │ Company    │ │ RabbitTemplate │
│ Plus CRUD │ │ Position   │ │ RedisTemplate  │
└───────────┘ └────────────┘ └────────────────┘
```

---

## 部署架构

```bash
# Docker Compose 一键启动基础设施
docker compose up -d
# 启动：MySQL + Redis + Nacos + RabbitMQ + MinIO

# 各微服务本地运行（按顺序）
mvn spring-boot:run -pl offer-gateway
mvn spring-boot:run -pl offer-auth
mvn spring-boot:run -pl offer-user
mvn spring-boot:run -pl offer-application
mvn spring-boot:run -pl offer-ai
```

所有基础设施容器化，微服务本地编译运行。后续可打包为镜像统一部署。

---

## 关键技术决策

| 决策 | 方案 | 原因 |
|------|------|------|
| 服务间不设分布式事务 | 最终一致性（RabbitMQ） | 跨库写场景极少，不需要 Seata |
| 公司/岗位名跨库查询 | Feign + Redis 缓存 | 避免冗余字段不一致 |
| Offer 和 Interview 嵌套路由 | `/api/applications/{id}/interviews` | 业务从属关系清晰 |
| Application 反范式冗余 | company_id / position_id 冗余 | 免跨库 JOIN，统计性能好 |
| MinIO 不走网关 | 预签名 URL 直连 | 大文件传输不压网关 |
| MQ 异常不阻断 | try-catch 兜底 | 保证主流程可用性 |
| PDF 文本提取 | Apache PDFBox，上传时自动提取存库 | LLM 非多模态，需先提取文本才能分析 |
| 提取异常不阻断上传 | try-catch 兜底，content_text=null | 保证上传主流程不受影响 |
