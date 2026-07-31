# OfferPilot — 求职管理平台 🚀

> 个人开发者独立完成的微服务全栈项目，覆盖求职全生命周期管理。

[![CI](https://github.com/ResoundingLion/offer-pilot/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/ResoundingLion/offer-pilot/actions/workflows/ci.yml)

---

## 📌 项目简介

OfferPilot 是一款面向求职者的全流程管理平台，帮助用户追踪投递进度、管理面试安排、跟进 Offer 决策。采用 Spring Cloud Alibaba 微服务架构，从 0 到 1 独立完成系统设计、编码实现与部署验证。

> **个人项目 · 持续更新中**

---

## 🛠️ 技术栈

| 层次 | 技术 | 版本 |
|------|------|:----:|
| 基础框架 | Java 17, Spring Boot 3.2.x, Spring Cloud Alibaba 2023.0.x | ✅ |
| 注册/配置中心 | Nacos 2.3.x | ✅ |
| 网关 | Spring Cloud Gateway | ✅ |
| ORM | MyBatis-Plus 3.5.x | ✅ |
| 数据库 | MySQL 8.0 | ✅ |
| 缓存 | Redis 7.x + spring-boot-starter-data-redis | ✅ |
| 服务调用 | OpenFeign + LoadBalancer | ✅ |
| 服务保护 | Sentinel（熔断降级 + FallbackFactory） | ✅ |
| 认证 | JWT + Spring Security | ✅ |
| 消息队列 | RabbitMQ（Topic Exchange + 状态变更事件） | ✅ |
| 对象存储 | MinIO（预签名 URL + 头像 OSS 上传） | ✅ |
| AI | LLM API（Anthropic 兼容接口，可切换提供商） | ✅ |
| API 文档 | Knife4j 4.5.x（待接入） | 📌 |
| 测试 | JUnit 5 + Mockito + AssertJ | ✅ |
| CI | GitHub Actions + JaCoCo 覆盖率 | ✅ |
| 部署 | Docker Compose（MySQL/Redis/Nacos/RabbitMQ/MinIO） | ✅ |

---

## 📐 架构图

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

---

## 🗂️ 模块说明

| 模块 | 说明 | API 数量 |
|------|------|:--------:|
| **offer-common** | 公共模块：统一响应、异常处理、自动填充、MinIO 文件服务 | — |
| **offer-api** | Feign 接口定义（跨服务通信契约 + 共享 DTO） | — |
| **offer-gateway** | 网关服务：路由转发、统一 JWT 鉴权、跨域配置 | — |
| **offer-auth** | 认证服务：注册、登录、JWT 签发 | 2 |
| **offer-user** | 用户/公司/岗位/简历 CRUD + 文件上传/下载 + 内部 Feign 接口 | 20+ |
| **offer-application** | 投递/面试/Offer 全流程管理 + Pipeline 流水线 + 一键推进 + RB 状态变更事件 | 16 |
| **offer-ai** | **AI Agent（ReAct + Tool Calling + 对话持久化）** | **5 (chat + agent + conversations)** |
| **offer-notification** | **通知服务：MQ 消费状态变更 → 站内信 + 邮件 + 顶栏铃铛** | **4 (list + unread-count + read + read-all)** |

---

## 🚀 项目亮点

- **微服务架构** — 6 个独立微服务，Nacos 注册发现 + 配置中心，Gateway 统一网关
- **Redis 缓存** — Cache-Aside 模式 + 随机 TTL 防雪崩 + 空值缓存防穿透
- **Sentinel 熔断降级** — 50% 异常比例 / 10s 统计窗口 / 30s 熔断 + 友好降级文案
- **RabbitMQ 异步解耦** — Topic Exchange + 投递状态变更事件 + **通知服务独立消费落库**，主流程零感知（2026-07-31）
- **通知服务** — MQ 驱动站内信 + 邮件（日志模拟），顶栏铃铛未读红点 + 全部已读（2026-07-31）
- **MinIO 对象存储** — 预签名 URL 直连 + 默认头像自动绑定
- **AI 求职 Agent** — ReAct 多轮 Tool Calling：JD 智能分析 / 投递分析 / 面试助手（2026-07-30）
- **Pipeline 流水线** — Dashboard 可视化阶段灯，一眼看清全部投递进度
- **一键推进** — 一个弹窗同时完成状态变更 + 面试/Offer 记录创建
- **77 单元测试全绿** — Mockito + JUnit 5 + AssertJ，Service 层全覆盖（55 + 通知服务 22）
- **GitHub Actions CI** — push 自动编译 + 跑测试 + JaCoCo 覆盖率报告

---

## 📋 核心领域模型

| 模型 | 关联 | 说明 |
|------|------|------|
| User | — | 用户信息（昵称/邮箱/手机/头像） |
| Company | user_id | 公司信息 |
| Position | company_id | 岗位（含薪资/城市/学历要求） |
| Application | user_id, company_id, position_id | 投递记录（状态机 + Pipeline） |
| Interview | application_id | 面试记录（多轮次） |
| Offer | application_id | Offer（薪资/奖金/股票 + 状态） |
| Resume | user_id | 简历（多版本，`title + version` 联合唯一） |
| **Conversation** | **user_id** | **AI Agent 对话会话 + 消息历史** |

### 状态流转

```
SAVED ──→ APPLIED ──→ ONLINE_ASSESSMENT ──→ INTERVIEW ──→ HR_INTERVIEW ──→ OFFER ──→ ACCEPTED
                                                                                        ↓
                                    REJECTED ←───────────────────────────────────── DECLINED
                                    WITHDRAWN (任意状态均可撤回)
```

> 非法流转被拒绝（如 SAVED 不能直接跳到 OFFER），终止态（REJECTED/WITHDRAWN）不可再变更。

---

## 🔑 核心功能

### 跨服务数据组装
```
GET /api/applications → 返回：
{
  "companyId": 5,
  "companyName": "字节跳动",     ← Feign + Redis 缓存
  "positionId": 5,
  "positionTitle": "后端开发工程师" ← Feign + Redis 缓存
}
```

### AI 求职 Agent（✅ 已完成 2026-07-30）

区别于简单的 AI 聊天，OfferPilot 的 AI 是真正的 **ReAct Agent**——LLM 自主决策调用工具获取数据、多步推理、综合分析：

- **JD 智能分析** — 粘贴岗位描述 → Agent 自动获取简历 → 技能匹配度分析 → 学习建议
- **AI 投递分析** — "看看我的求职情况" → Agent 多步推理：先查统计 → 拒信多深挖面试反馈 → 综合优化建议
- **AI 面试助手** — 结合简历技能生成面试题和准备建议

```
POST /api/ai/agent → JD 分析 / 面试准备 / 投递建议
{"message": "帮我分析这个 JD：精通 Java、Spring Cloud..."}

Agent 工作流：
① 调 get_active_resume 拿简历 → ② 分析 JD 关键词 → ③ 技能匹配 → ④ 输出
```

### 文件上传
```
POST /api/files/upload → MinIO 预签名 URL
用户头像 → 选图 → 上传 MinIO → 自动填路径 → 保存
简历 PDF → 上传 MinIO → PDFBox 自动提取文本 → 存入 resume.content_text → 供 AI 分析
```

---

## ⚙️ 快速启动

### 前置条件

- JDK 17+
- Docker & Docker Compose
- Maven 3.8+

### 1. 启动基础设施

```bash
git clone https://github.com/ResoundingLion/offer-pilot.git
cd offer-pilot
docker compose up -d
# 启动 MySQL / Redis / Nacos / RabbitMQ / MinIO
```

### 2. 导入 Nacos 配置

浏览器打开 `http://localhost:8848/nacos`（账号/密码：nacos/nacos），导入 `nacos-config` 目录下的配置文件。

### 3. 执行数据库脚本

执行 `sql/` 目录下建表脚本，创建 3 个库 8 张表。

### 4. 启动微服务

```bash
mvn spring-boot:run -pl offer-gateway       # 端口 8080
mvn spring-boot:run -pl offer-auth          # 端口 8081
mvn spring-boot:run -pl offer-user          # 端口 8082
mvn spring-boot:run -pl offer-application   # 端口 8083
mvn spring-boot:run -pl offer-ai            # 端口 8084（需配置 AI API Key）
mvn spring-boot:run -pl offer-notification  # 端口 8085（消费 MQ 状态变更 → 通知）
```

### 5. 验证

```bash
# 注册
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456"}'
```

---

## 📈 项目路线图

| Sprint | 内容 | 状态 |
|:------:|------|:----:|
| Sprint 1-8 | 基础架构 → 全功能后端 + 前端 + 测试 + Redis/MQ/MinIO/AI | ✅ |
| Sprint 9 | Resume 简历管理（多版本） | ✅ |
| Sprint 10 | AI Agent 升级（JD分析/面试助手/投递分析） | ✅ |
| Sprint 11 | Notification 通知服务（MQ 站内信 + 邮件 + 顶栏铃铛） | ✅ |
| Sprint 12 | Controller 测试 + 日志聚合 + Knife4j + Arthas 调优 | 待开始 |

> 详细路线见 [ROADMAP.md](ROADMAP.md)。

---

## 📚 文档索引

| 文档 | 说明 |
|------|------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | 微服务架构设计 |
| [API_SPEC.md](API_SPEC.md) | REST API 详细规范 |
| [DATABASE.md](DATABASE.md) | 数据库设计（表结构 + 索引 + 状态流转） |
| [ROADMAP.md](ROADMAP.md) | 开发路线图（Sprint 排期） |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志 |
