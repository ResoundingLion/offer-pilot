# Changelog

所有重要变更均记录在此文件。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-cn/)。

## [1.7.0-SNAPSHOT] — 2026-07-31

### 📨 Notification 通知服务

- **新增 offer-notification 微服务**（端口 8085）：消费投递状态变更事件 → 生成站内信 + 发邮件，顶栏铃铛查看
- **新增 offer_notification 库 + notification 表**：站内信持久化，`idx_user_read` / `idx_user_created` 索引
- **跨服务事件共享**：`ApplicationEvent` 从 offer-application 搬到公共模块 offer-api（新增 companyName / positionTitle），新增 `MqConstants` 统一交换机/队列/路由键
- **消费链路**：`NotificationConsumer`（@RabbitListener）→ `NotificationMessageBuilder`（按状态生成文案）→ `NotificationServiceImpl`（落库）→ `MailService`（日志模拟发邮件，预留 SMTP 配置）
- **4 个 REST 接口**：`GET /api/notifications`（列表）、`GET /unread-count`（未读数）、`PUT /{id}/read`（单条已读）、`PUT /read-all`（全部已读）
- **前端顶栏铃铛**：未读红点数字 + 下拉通知列表 + 全部已读（layout.vue + notification.js）
- **Nacos 配置**：新增 `offer-notification.yaml`（datasource + rabbitmq + 邮件预留）；`offer-gateway.yaml` 新增 `/api/notifications/**` 路由
- **22 个新测例**：NotificationServiceImplTest（8）+ NotificationMessageBuilderTest（11）+ NotificationConsumerTest（3），总 77 测例全绿
- **文档同步**：README / ARCHITECTURE / DATABASE / ROADMAP 全部更新

---

## [1.6.0-SNAPSHOT] — 2026-07-30

### 🤖 AI Agent 升级（ReAct + Tool Calling）

- **ReAct Agent 核心**：`AgentService.java` — for 循环多轮 Tool Calling（最多 5 轮），LLM 自主决策推理→行动→观察→再推理
- **工具系统**：`ToolDefinitionProvider`（5 个工具定义）+ `ToolExecutor`（switch-case 路由 Feign 调用）
- **工具清单**：`get_active_resume`（简历）、`get_dashboard_stats`（统计）、`get_applications`（投递列表）、`get_interviews`（面试记录）、`get_offer`（Offer）

### 📦 offer-api 新增 Feign 接口

- **4 个 Feign Client**：ResumeClient、ApplicationClient、InterviewClient、OfferClient（含 FallbackFactory）
- **4 个共享 DTO**：ResumeDTO、ApplicationDTO、InterviewDTO、OfferDTO

### 🔌 内部 Controller 新增

- **offer-user**：`ResumeInternalController`（`/internal/resumes/active`）
- **offer-application**：`ApplicationInternalController`、`InterviewInternalController`、`OfferInternalController`

### 🗄️ 对话持久化

- **数据库**：`offer_ai` 库 + `conversation` / `conversation_message` 2 张 MySQL 表
- **实体**：Conversation、ConversationMessage + 对应 Mapper
- **ConversationService**：建对话/保存消息/加载历史/删除对话
- **Nacos offer-ai.yaml**：新增 datasource 配置 + `spring.main.allow-bean-definition-overriding: true`

### 🎨 前端 Agent 界面

- **双栏布局**：左侧历史对话列表（新建/切换/删除）+ 右侧聊天区
- **Markdown 渲染**：`marked` 库渲染 LLM 返回的结构化分析结果
- **三个快捷入口**：JD 分析/投递分析/面试助手，带引导 prompt
- **历史续聊**：切换对话自动加载历史消息，LLM 保持上下文

### 🐛 修复

- **创建简历时 PDF 文本不自动提取**：`ResumeServiceImpl.create()` 插入后检测 fileUrl 是否为 PDF，自动从 MinIO 下载→PdfService 提取→写入 contentText，创建弹窗也生效
- **JSONObject.toMap() 编译报错**：Hutool 5.8.32 的 JSONObject 无 toMap() 方法，改为直接强转 Map

---

## [1.5.0-SNAPSHOT] — 2026-07-29

### 📄 Resume 简历管理模块（多版本）

- **新增 resume 表**：`offer_user` 库，`title + version` 联合唯一，`isCurrent` 标记当前版本
- **后端 7 接口**：GET（列表/详情）、POST（创建）、PUT（编辑）、DELETE（删除）、PATCH（新版本/切换当前）
- **多版本逻辑**：同 title 下 version 自增（CREATE-ON-WRITE），`setCurrent` 两步行事务保证一致性
- **前端简历页面**：按标题分组卡片、创建/编辑弹窗、新版本一键复制、切换当前版本
- **选文件即上传**：创建弹窗内选文件→立即上传 MinIO，填表单时间遮挡上传等待
- **PDF 预览**：fetch + Authorization header 下载 → blob URL → `<embed>` 渲染，绕过 JWT 认证限制
- **文件上传增强**：`FileController` 新增 `type` 参数（`avatar/` / `resume/` 路径隔离）
- **Nacos 配置**：`offer-user.yaml` 新增 `spring.servlet.multipart`（max 10MB）；`offer-gateway.yaml` 新增 `/api/resumes/**` 路由
- **文档同步**：ROADMAP.md / README.md / ARCHITECTURE.md / DATABASE.md / API_SPEC.md / 前端 README.md 全部更新

### 📄 PDF 简历文本提取（供 AI 分析）

- **PdfService**：Apache PDFBox 3.0.3，`InputStream → byte[] → PDDocument → 纯文本`，异常安全
- **Resume Entity 新增字段**：`content_text MEDIUMTEXT`（上限 16MB），存 PDF 提取的纯文本内容
- **自动提取**：`FileController.upload()` 新增可选参数 `resumeId`，type=resume + PDF 文件时上传完毕自动提取文本入库
- **手动提取接口**：`POST /api/resumes/{id}/extract-text`，供创建弹窗流程补提取，或手动重新触发
- **前端适配**：`uploadFile(file, 'resume', resumeId)` 上传时携带 resumeId 自动触发生成
- **上传不阻断**：提取异常捕获，失败仅 `content_text=null`，上传和业务流程不受影响

---

## [1.4.0-SNAPSHOT] — 2026-07-28

### 🤖 offer-ai 智能助手微服务

- **新建微服务**：`offer-ai`（端口 8084），注册 Nacos
- **Gateway 路由**：`/api/ai/**` 转发到 offer-ai
- **Nacos 配置**：`offer-ai.yaml`（端口 + AI API 参数：base-url/api-key/model）
- **AiProperties.java**：`@ConfigurationProperties` 读取 AI 配置
- **AiService.java**：RestTemplate 调 LLM API（Anthropic 兼容接口，x-api-key 认证）
- **AiController.java**：`POST /api/ai/chat` 对话接口
- **前端 AI 助手页面**：聊天界面 + 建议问题 + 消息流 + 打字动画
- **全链路验证通过**：直连 8084 + 走网关 + 带 Token 认证 + 真实 AI 回复

### 🐛 修复

- **DataSource 自动配置冲突**：AI 服务依赖 offer-common（含 MyBatis-Plus），启动类排除 DataSourceAutoConfiguration

---

## [1.3.0-SNAPSHOT] — 2026-07-28

### 🧪 单元测试扩充至 55 测例

- **新增 OfferServiceImplTest（13 测例）**：CRUD 7 + 状态流转 6（PENDING→ACCEPTED/DECLINED 合法/非法/不存在/无效字符串）
- **新增 CacheServiceTest（8 测例）**：缓存命中 2 + 未命中 2 + 空值防穿透 2 + evict 2
- **修复 RabbitTemplate 编译报错**：`ApplicationServiceImplTest` 构造函数补传 RabbitTemplate
- 合计 55 测例全绿通过

### 📨 RabbitMQ 消息队列

- **Docker 容器**：docker-compose 新增 rabbitmq:3-management（5672 + 15672 端口）
- **依赖配置**：父 POM + offer-application 添加 spring-boot-starter-amqp
- **新增配置类**：`RabbitMQConfig.java` — TopicExchange + Queue + Binding + Jackson2JsonMessageConverter
- **新增消息体**：`ApplicationEvent.java` — 状态变更事件（applicationId/oldStatus/newStatus/currentStage/userId/timestamp）
- **新增消费者**：`StatusChangeConsumer.java` — @RabbitListener 异步消费 + 日志打印
- **修改 Service**：`ApplicationServiceImpl.java` — advance() 末尾发送 MQ 消息，try-catch 防阻断主流程，加 @Slf4j
- **Nacos 配置**：配置中心发布 RabbitMQ 连接信息
- **全链路联调验证通过**：控制台打印 `📨 [状态变更]` 消息

### 🐛 修复

- **跳过中间阶段 Bug**：未勾选测评/笔试时从 APPLIED 推进到 INTERVIEW 被状态机拦截，新增 `isTransitionAllowedByPipeline()` 放行逻辑

---

## [1.2.0-SNAPSHOT] — 2026-07-27

### 📋 路线全面修订

- **全部规划内功能一个不漏**：用户确认时间充裕，所有遗漏功能全部补上
- 新增 3 项遗漏：MinIO 文件存储、RabbitMQ 消息队列、智能助手 offer-ai
- 恢复 2 项遗漏：Resume 简历管理、Controller 层测试
- 保留原有规划：Knife4j、Arthas 调优、全链路验证
- 更新 ROADMAP.md 为七阶段路线

---

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
