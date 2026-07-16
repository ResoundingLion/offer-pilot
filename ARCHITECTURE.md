# OfferPilot 微服务架构设计

## 架构总览

```
Client (浏览器 / Postman)
    │
    ▼
┌─────────────────────────────────────────────┐
│         Spring Cloud Gateway (8080)         │
│  ┌──────────┐ ┌──────────┐ ┌─────────────┐  │
│  │ Auth 过滤 │ │ 路由转发  │ │ 跨域/限流   │  │
│  └──────────┘ └──────────┘ └─────────────┘  │
└──────────────┬──────────────────────────────┘
               │
    ┌──────────┼──────────────┐──────────┐
    ▼          ▼              ▼          ▼
┌────────┐ ┌────────┐ ┌────────────┐ ┌────────────┐
│  Auth  │ │  User  │ │ Application│ │Notification│ ← MVP后做
│ (:8081)│ │(:8082) │ │  (:8083)   │ │  (:8084)   │
└───┬────┘ └───┬────┘ └──────┬─────┘ └────────────┘
    │          │              │
    └──────────┴──────┬───────┘
                      ▼
             ┌────────────────┐
             │  Nacos Server   │ 注册中心 + 配置中心
             │  local:8848     │
             └────────────────┘
```

## 微服务划分（MVP 阶段）

| 服务 | 端口 | 职责 | 数据库 |
|------|------|------|--------|
| offer-gateway | 8080 | 路由转发、统一鉴权、跨域 | 无 |
| offer-auth | 8081 | 登录/注册、JWT 签发与校验 | offer_auth |
| offer-user | 8082 | 用户管理、公司管理、岗位管理 | offer_user |
| offer-application | 8083 | 投递管理、面试管理、Offer 管理 | offer_application |

> **为什么 MVP 只做 4 个：** 10 个微服务一个人开发，光环境配置就消耗大半时间。公司本质上依赖用户，岗位依赖公司，投递/面试/Offer 属于同一条业务线——拆到 4 个是合理的。notification 没有强依赖业务方，延后。

## 服务间调用

同步调用使用 OpenFeign（同库内完成，无分布式事务需求）：
```
offer-auth → offer-user  (校验用户状态)
offer-application → offer-user  (获取岗位/公司信息)
```

异步消息（MVP 阶段预留接口，暂不实现）：
```
offer-application → RabbitMQ → offer-notification
```

## 注册中心 / 配置中心

- 组件：Nacos 2.x
- 地址：`localhost:8848`
- 每个服务注册到 Nacos，通过服务名互相发现
- 配置文件统一放置在 Nacos 上，本地仅保留 bootstrap.yml

## 统一认证方案

```
请求 → Gateway → Auth 过滤器校验 Token
    → 无 Token / Token 过期 → 返回 401
    → Token 有效 → 转发到目标服务（Header 携带 userId）
```

- 使用 JWT（access_token + refresh_token）
- 服务之间通过 OpenFeign 调用时不透传 Token，改用内部鉴权 Header

## 部署架构（MVP）

```
Docker Compose 一键启动：
  nacos         → nacos/nacos-server:latest
  mysql         → mysql:8.0
  redis         → redis:7
  offer-gateway → 本地构建
  offer-auth   → 本地构建
  offer-user   → 本地构建
  offer-application → 本地构建
```

## 后续扩展方向

- offer-notification 独立部署（邮件 + WebSocket）
- offer-analytics 独立部署（数据分析）
- offer-ai 独立部署（AI 功能）
- 新增服务只需注册到 Nacos + 在 Gateway 添加路由

### 要点说明

为什么服务间不用分布式事务？投递→创建面试→Offer 都在同一个服务里（offer-application），面试和 Offer 不涉及跨库更新。后续如果出现跨服务写场景，用 RabbitMQ 最终一致性解决，不需要 Seata。
