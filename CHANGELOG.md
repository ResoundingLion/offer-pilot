# Changelog

所有重要变更均记录在此文件。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [1.0.0-SNAPSHOT] — 2026-07-19

### 新增

#### 🔧 跨服务 Feign 调用
- 新增 `offer-api` 模块 Feign 接口定义：`CompanyClient`、`PositionClient`
- 新增共享 DTO：`CompanyDTO`、`PositionDTO`（轻量级，仅含跨服务所需字段）
- 新增 offer-user 内部控制器：`CompanyInternalController`、`PositionInternalController`
  - 路径 `/internal/companies/{id}`、`/internal/positions/{id}`
  - 不经过网关，不走 `Result` 包装，直接返回 DTO
- ApplicationVO 增加 `companyName`、`positionTitle` 字段
- 新增 `ApplicationService.enrichVO()` 通过 Feign 跨服务组装完整 VO
- ApplicationController 全部接口使用 `enrichVO` 替代直接转换

### 修复

- **Feign Bean 冲突**：为 `CompanyClient` 和 `PositionClient` 添加 `contextId` 区分，解决同 `name` 冲突
- **内部接口返回 null 500 错误**：内部 Controller 找不到数据时返回空 DTO 而非 null，避免 Feign 反序列化异常
- **依赖缺失**：offer-application 添加 `spring-cloud-starter-loadbalancer` 依赖

### 重构

- Converter 类全部改为静态方法，移除 `@Component` 注解
- Controller 直接从字段注入改为类名直接调用静态方法

---

## [0.8.0-SNAPSHOT] — 2026-07-17

### 新增

- offer-application 模块 DTO / VO / Controller 全部完成（10 个文件）
- Application 6 个接口（CRUD + 状态流转）
- Interview 4 个接口（CRUD）
- Offer 4 个接口（CRUD + 状态流转）
- 投递状态机流转规则（7 种状态，非法流转校验）

### 修复

- MetaObjectHandler 自动填充统一方案（`OfferPilotMetaObjectHandler` + `@Bean` 注入）
- 三模块 MybatisPlusConfig 统一为 `@Bean` 方案

---

## [0.5.0-SNAPSHOT] — 2026-07-15

### 新增

- offer-user 模块全部完成：User/Company/Position 三层 + DTO + Controller + VO（23 个文件）
- 三个模块共 15 个 REST 接口测试通过
- offer-auth 认证服务（注册/登录 + JWT 签发 + Spring Security）

### 基础设施

- 父 POM 搭建（Spring Boot 3.2.12 + Spring Cloud Alibaba 2023.0.3.4）
- Docker 容器：MySQL/Redis/Nacos 正常运行
- Nacos 配置中心：5 个配置已上传
- SQL 建表脚本：3 个库 7 张表
- Git 仓库初始化 + 推送 GitHub
