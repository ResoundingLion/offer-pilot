# Changelog

所有重要变更均记录在此文件。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [1.1.0-SNAPSHOT] — 2026-07-19

### 🎨 深色科技风 UI 增强

- **全局扫描线叠加**：固定层青色线性扫描，增强赛博氛围
- **页面转场动画**：Vue `<Transition name="page-fade">` — 进入时模糊+位移，退出时淡出
- **粒子登录页**：30 颗浮动发光粒子 + 呼吸网格 + 光晕脉冲 + 按钮扫光
- **输入框暗色调**：从白色底改为青色暗底 `rgba(0,212,255,0.02)`，青色发光光标 + hover 光晕
- **分页箭头赛博化**：hover 放大 + 辉光 + 激活态青色边框
- **卡片顶部描边**：hover 时顶部渐变辉光出现
- **弹窗动画**：scale + translateY 入场，遮罩模糊
- **NProgress 赛博条**：青紫渐变 + 发光

### 🐛 修复

- **`<Transition>` 多根节点 Bug**：`application/index.vue` 模板 3 个平级根节点导致路由切换卡死，统一包一层 `<div>`
- **表单描述列表白色背景**：Element Plus `el-descriptions` 白色底色改为暗色透明

### 🏗️ Pipeline 投递进度流水线

- **数据库新增**：`application` 表 `pipeline_config` + `current_stage` 字段
- **后端**：
  - `PipelineVO.java` — 流水线 VO（applicationId + stages 阶段灯列表）
  - `DashboardController` 新增 `GET /api/applications/dashboard/pipeline`
  - 推导算法：优先用 `currentStage` 定位阶段，兼容旧数据反推
  - 5 种灯状态：COMPLETED/ACTIVE/PENDING/FAILED/WITHDRAWN
- **前端**：
  - Dashboard 统计卡片与图表之间插入 Pipeline 流水线区域
  - 水平阶段灯条：🟢 ✅ / 🔵 ◉ / ⚪ / 🔴 ✕ / ⚫ ✕
  - 悬浮 tooltip 显示阶段状态，点击跳投递详情

### 🚀 一键推进

- **后端**：
  - `AdvanceRequest.java` — 推进 DTO（targetStage + 面试字段 + Offer 字段）
  - `ApplicationController` 新增 `PATCH /{id}/advance`
  - 一个请求同时处理：状态变更 + 面试/Offer 记录创建
  - 子阶段推进（ASSESSMENT→EXAM）：`current_stage` 精确记录，同状态内也更新时间戳
  - Offer 占位：不填薪资只记 `current_stage`，不改 `application.status`
  - 补结果逻辑：推进到已有面试记录的轮次 → 更新面试而非新建
- **前端**：
  - 每条 Pipeline 记录右侧「推进→」按钮（青紫渐变 + 辉光）
  - 动态弹窗：面试轮次显示面试表单，Offer 显示薪资表单，简单阶段显示确认
  - 青色阶段再点推进 → 弹出编辑框补结果
  - 提交后 Dashboard 自动刷新 Pipeline

### 📝 新投递流程配置

- 新增/编辑投递弹窗新增「流程配置」区域，可勾选：测评/笔试/二面/三面/四面
- 实时流程预览（固定阶段青色 + 可选阶段灰色）

---

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
