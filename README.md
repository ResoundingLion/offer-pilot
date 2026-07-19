# OfferPilot — 求职管理平台 🚀

> 个人开发者独立完成的微服务全栈项目，覆盖求职全生命周期管理。

## 📌 项目简介

OfferPilot 是一款面向求职者的全流程管理平台，帮助用户追踪投递进度、管理面试安排、跟进 Offer 决策。采用 Spring Cloud Alibaba 微服务架构，从 0 到 1 独立完成系统设计、编码实现与部署验证。

**核心亮点：**
- 🏗️ **微服务架构** — 4 个独立服务，Nacos 注册发现 + 配置中心
- 🔐 **统一认证** — JWT 令牌 + Spring Security + 网关全局鉴权
- 🔄 **状态机驱动** — 投递全生命周期（SAVED → APPLIED → ... → OFFER/REJECTED）含非法流转校验
- 🌐 **OpenFeign 跨服务调用** — Application 服务跨服务查询 Company/Position 信息
- 🎯 **Pipeline 投递进度流水线** — Dashboard 可视化阶段灯，一眼看清全部投递状态
- 🚀 **一键推进** — 一个弹窗同时完成状态变更 + 面试/Offer 记录创建
- 🎨 **深色科技风 UI** — 粒子登录页、赛博卡片、扫描线、页面转场动画
- 📦 **Docker 容器化** — MySQL/Redis/Nacos 一键部署

## 🛠️ 技术栈

| 层次 | 技术 |
|------|------|
| 基础框架 | Java 17, Spring Boot 3.2.x, Spring Cloud Alibaba 2023.0.x |
| 注册/配置中心 | Nacos 2.3.x |
| 网关 | Spring Cloud Gateway |
| ORM | MyBatis-Plus 3.5.x |
| 数据库 | MySQL 8.0, Redis 7.x |
| 服务调用 | OpenFeign + LoadBalancer |
| 认证 | JWT + Spring Security |
| API 文档 | Knife4j (预留) |
| 部署 | Docker Compose |

## 📐 架构图

```
Client (浏览器 / Postman)
    │
    ▼
┌─────────────────────────────────────┐
│    Spring Cloud Gateway (8080)      │
│  路由转发 + JWT 鉴权 + 跨域        │
└──────────┬──────────────────────────┘
           │
    ┌──────┼──────────┬──────────┐
    ▼      ▼          ▼          ▼
┌──────┐ ┌──────┐ ┌────────┐ ┌────────────┐
│ Auth │ │ User │ │ Appl.  │ │ Notification│
│:8081 │ │:8082 │ │:8083   │ │  (MVP后)   │
└──┬───┘ └──┬───┘ └───┬────┘ └────────────┘
   └───────┬┴─────────┘
           ▼
    ┌──────────────┐
    │ Nacos Server │ 注册中心 + 配置中心
    │ :8848       │
    └──────────────┘
```

## 🗂️ 模块说明

| 模块 | 说明 | API 数量 |
|------|------|---------|
| **offer-gateway** | 网关服务：路由转发、统一 JWT 鉴权、跨域配置 | — |
| **offer-auth** | 认证服务：注册、登录、JWT 签发 | 2 |
| **offer-user** | 用户/公司/岗位 CRUD | 15 |
| **offer-application** | 投递/面试/Offer 全流程管理 + Pipeline 流水线 + 一键推进 | 16 |
| **offer-common** | 公共模块：统一响应、异常处理、自动填充 | — |
| **offer-api** | Feign 接口定义（跨服务通信契约） | — |

## ⚙️ 快速启动

### 前置条件

- JDK 17+
- Docker & Docker Compose
- Maven 3.8+

### 1. 启动基础设施

```bash
git clone https://github.com/ResoundingLion/offer-pilot.git
docker compose up -d
# 启动 MySQL、Redis、Nacos
```

### 2. 导入 Nacos 配置

浏览器打开 `http://localhost:8848/nacos`（账号/密码：nacos/nacos），导入 `nacos-config` 目录下的 5 个配置文件。

### 3. 执行数据库脚本

执行 `sql/` 目录下建表脚本，创建 3 个库 7 张表。

### 4. 启动微服务

按顺序启动：

```bash
mvn spring-boot:run -pl offer-gateway    # 端口 8080
mvn spring-boot:run -pl offer-auth       # 端口 8081
mvn spring-boot:run -pl offer-user       # 端口 8082
mvn spring-boot:run -pl offer-application # 端口 8083
```

### 5. 验证

```bash
# 注册
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"123456"}'

# 获取 token → 调用各业务接口
```

## 🔑 核心功能

### 投递状态机

```
SAVED → APPLIED → ONLINE_ASSESSMENT → INTERVIEW → HR_INTERVIEW → OFFER
  │         │            │              │             │             │
  └─ WITHDRAWN ←─────────┴───── REJECTED ─────────────┘             │
                                                                    │
                                                            ACCEPTED / DECLINED
```

非法流转被拒绝（如 SAVED 不能直接跳到 OFFER），终止态（REJECTED/WITHDRAWN）不可再变更。

### 跨服务数据组装

```
GET /api/applications → 返回：
{
  "companyId": 5,
  "companyName": "字节跳动",    ← Feign 跨服务查询 offer-user
  "positionId": 5,
  "positionTitle": "后端开发工程师" ← Feign 跨服务查询 offer-user
}
```

## 📈 项目路线图

| 阶段 | 内容 | 状态 |
|------|------|------|
| Sprint 1 | 脚手架搭建 + 核心业务 CRUD | ✅ 完成 |
| Sprint 2 | 投递状态机 + 面试/Offer 管理 | ✅ 完成 |
| Sprint 3 | 跨服务 Feign 调用 | ✅ 完成 |
| Sprint 4 | UI 赛博改造 + Pipeline 流水线 + 一键推进 | ✅ 完成 |
| Sprint 5 | 单元测试 + GitHub Actions CI | ⬜ 待开始 |
| Sprint 6 | Docker 容器化部署 + CI/CD | ⬜ 待开始 |

## 📚 文档索引

| 文档 | 说明 |
|------|------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | 微服务架构设计 |
| [API_SPEC.md](API_SPEC.md) | REST API 详细规范 |
| [DATABASE.md](DATABASE.md) | 数据库设计（ER 图/索引/约束） |
| [PROJECT.md](PROJECT.md) | 技术栈与依赖说明 |
| [ROADMAP.md](ROADMAP.md) | 开发路线图 |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志 |

---

> **个人项目 · 持续更新中**
> 如有问题或建议，欢迎提交 Issue 或 PR。
