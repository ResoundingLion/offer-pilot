# OfferPilot 开发路线图

目标：秋招前交付一个安全边界清楚、可从零启动、主链路可重复验证的学习型项目。功能数量不再作为主要目标。

---

## ✅ 已完成

| Sprint | 时间 | 内容 |
|--------|:----:|------|
| Sprint 1 | 07-13 | 脚手架搭建（父 POM、Docker、Nacos、Gateway、Auth） |
| Sprint 2 | 07-14 | 用户 + 公司 + 岗位 CRUD（15 接口） |
| Sprint 3 | 07-15 | 投递 + 面试 + Offer + 状态机（15 接口）+ 自动填充 + Feign 跨服务 |
| Sprint 4 | 07-19 | Vue 3 前端 7 页全功能 + Dashboard ECharts + Pipeline + 一键推进 + 深色科技风 |
| Sprint 5 | 07-23 | 单元测试（55 测例全绿）+ GitHub Actions CI + JaCoCo |
| Sprint 6 | 07-24 | Redis 缓存（Cache-Aside + 防雪崩/穿透）+ Sentinel 熔断降级 |
| Sprint 7 | 07-27 | MinIO 文件存储 + 头像 OSS 上传 + RabbitMQ 消息队列（Topic Exchange + 状态变更事件） |
| Sprint 8 | 07-28 | offer-ai 智能助手（LLM API + 前端聊天页面） |
| Sprint 9 | 07-29 | Resume 简历管理（多版本）+ PDF 文本提取 — 后端 8 接口 + 前端 + 上传即提取 |
| **Sprint 10** | **07-30** | **AI Agent 升级 —— ReAct + Tool Calling + Feign 跨服务数据查询 + 对话持久化 MySQL + 前端双栏 Agent 界面** |
| **Sprint 11** | **07-31** | **Notification 通知服务 —— MQ 状态变更 → 独立微服务消费 → 站内信 + 邮件 + 顶栏铃铛（22 新测例，总 77 全绿）** |

## 🎯 路线共识（2026-07-31）

### 项目定位

- OfferPilot 是个人学习型秋招项目，不按生产 SaaS 的安全与运维规模建设。
- 当前功能面已经足够，下一阶段停止堆叠技术栈，优先补齐资源归属、可复现启动和真实链路验证。
- 安全目标是消除明显越权并形成可解释的最小闭环，不引入 Keycloak、完整 RBAC、mTLS 或独立权限平台。

### 范围与优先级

| 级别 | 内容 | 处理原则 |
|------|------|----------|
| **必须完成** | 全域资源归属校验 | Company、Position、Resume、Application、Interview、Offer、Conversation、Notification、File 均按当前 userId 隔离 |
| **必须完成** | 修复 Application 更新 ID 双轨 | 资源 ID 只使用路径参数，请求体不得决定被更新对象 |
| **必须完成** | AI 与文件安全 | Agent 内部查询按 resourceId + userId 过滤；Markdown 渲染净化；头像/简历限制类型、大小和归属 |
| **必须完成** | 安全回归测试 | 优先覆盖“用户 B 操作用户 A 的资源 → 403/404”，不追求机械的 Controller 全覆盖 |
| **必须完成** | 仓库自举与真实 E2E | 配置模板、建库、种子数据、启动说明齐全；主链路可重复执行 |
| **收尾增强** | Agent 基础健壮性 | LLM 连接/读取超时、有限重试、历史与工具结果预算 |
| **收尾增强** | MQ 基础可靠性 | publisher confirm/mandatory、消费幂等、有限重试和 DLQ；事务 Outbox 记为技术债 |
| **技术债** | 生产级安全与运维 | Service JWT、mTLS、Keycloak、WAF、病毒扫描、Loki/Grafana、Arthas 暂不实施 |

### 三周执行路线

#### Week 1：最小可信安全闭环

1. 逐 Controller 建立资源归属矩阵，所有个人数据按 `resourceId + userId` 查询、更新和删除。
2. 修复 Application PUT 的 path/body ID 不一致；AI 内部工具查询同时携带并校验 userId。
3. 文件接口校验用户目录与 resumeId 归属，仅允许常见图片/PDF并限制大小；AI Markdown 使用 DOMPurify 净化。
4. 增加关键越权回归测试，每类高风险资源至少覆盖一条跨用户拒绝场景。

#### Week 2：可复现、可验证

1. 提交不含真实密钥的 Nacos 配置模板、建库脚本和演示种子数据。
2. 修正 README/ARCHITECTURE/API_SPEC 的服务、数据库、状态机和启动步骤漂移。
3. 建立可重复 E2E：注册 → 公司/岗位 → 投递 → 推进 → 通知 → AI 分析。
4. Agent 增加 HTTP 超时、有限重试和上下文预算；消息序号并发问题记录并择机修复。

#### Week 3：可靠性与演示收尾

1. RabbitMQ 增加发布确认、消费幂等、有限重试和 DLQ；Outbox 不在本阶段实施。
2. 为 Gateway、offer-user、offer-ai 和文件链路补关键测试，CI 使用渐进式模块门禁。
3. 固化演示账号、演示脚本和面试讲解，确保一次完整演示稳定可重复。
4. 全量回归、文档最终对齐和 Bug 修复；Knife4j 仅在不影响主线时接入。

### 完成标准

- 两个不同用户之间不能读取、修改或删除彼此的个人资源。
- 新环境仅依据仓库文档即可启动基础设施和全部服务，不依赖未提交的本地知识。
- E2E 主链路可重复执行并留下可检查的结果；CI 不只证明 Mockito 分支逻辑通过。
- 演示中 AI 请求不会无限等待，通知失败路径有明确处理；生产级能力如 mTLS、Outbox、集中日志被诚实记录为技术债。

## 🗃️ 历史规划（2026-07-30，已被上方共识取代）

以下内容仅保留迭代背景，不再作为当前执行顺序。

| 序 | 任务 | 面试价值 | 状态 |
|:--:|------|:--------:|:----:|
| 1️⃣ | Resume 简历管理（多版本） | 补领域模型，用户自用 | **✅ 已完成** |
| 2️⃣ | **AI Agent 升级（JD分析/投递分析/面试助手）** | 🔥 最大差异化 | **✅ 已完成** |
| 3️⃣ | **Notification 通知服务** | 圆 RabbitMQ 的故事 | **✅ 已完成（2026-07-31）** |
| 4️⃣ | **Controller 测试补漏** | 测试分层覆盖 | 待开始 |
| 5️⃣ | **Actuator + Loki 日志聚合** | 微服务运维故事 | 待开始 |
| 6️⃣ | **Knife4j + 种子数据 + 全链路验证** | 收尾展示 | 待开始 |
| 7️⃣ | **Arthas 性能调优** | 差异化亮点，有余力再执行 | 待开始 |

### 2. AI Agent 升级 🤖 ✅ 已完成（2026-07-30）

从"聊天 API 代理"升级为真正的求职 Agent：

- **ReAct Agent 核心**：for 循环多轮 Tool Calling（最多 5 轮），LLM 自主决策调哪些工具
- **JD 智能分析**：粘贴 JD → LLM 自主调 `get_active_resume` 获取简历 → 技能匹配度 → 学习建议
- **AI 投递分析**：多步推理——先调 `get_dashboard_stats` 看整体 → 拒信多就调 `get_applications` → 面试挂的多调 `get_interviews`
- **AI 面试助手**：结合简历技能生成面试题和准备建议
- **关键技术**：ReAct 循环、Tool Calling（Anthropic 兼容格式）、Feign 跨服务调用（6 个新 Client）、对话持久化（MySQL 2 表）、结构化 Markdown 输出
- **offer-api 新增**：4 个 Feign Client + 4 个 FallbackFactory + 4 个 DTO（Resume/Application/Interview/Offer）
- **内部 Controller 新增**：ResumeInternalController + 3 个 Application 域内部 Controller
- **offer-ai 架构**：`AgentService`（ReAct 循环）+ `ToolDefinitionProvider`（5 工具定义）+ `ToolExecutor`（Feign 路由）+ `ConversationService`（对话 CRUD）
- **前端升级**：双栏布局（历史对话列表 + 聊天区）、Markdown 渲染、快捷入口分组
- **学完能答**："Agent 和普通 LLM 调用有什么区别？Tool Calling 怎么实现的？"

### 3. Notification 通知服务 📨 ✅ 已完成（2026-07-31）

从"打日志的消费者"升级为真正的通知闭环：

- **新增 offer-notification 微服务**（端口 8085）+ `offer_notification` 库 + `notification` 表
- **跨服务事件共享**：`ApplicationEvent` 搬到公共模块 offer-api（新增公司名/岗位名字段），`MqConstants` 统一交换机/队列/路由键，消除魔法字符串
- **消费链路**：`NotificationConsumer` 监听状态变更 → `NotificationMessageBuilder` 按状态生成文案 → 落库站内信 → `MailService` 发邮件（日志模拟，预留 SMTP 配置）
- **4 个 REST 接口**：通知列表 / 未读数 / 单条已读 / 全部已读
- **前端顶栏铃铛**：未读红点数字 + 下拉通知列表 + 全部已读（layout.vue）
- **22 个新测例**：Service（8）+ 文案生成器（11）+ 消费者（3），总 77 测例全绿
- **学完能答**："MQ 在你们项目里到底解决了什么实际问题？"——异步解耦：通知服务独立落库+发邮件，投递主流程零感知、毫秒完成；通知服务故障隔离（消息留在队列等恢复）；MQ 天然削峰

### 4. Controller 层测试 🧪
- 每个 Controller 的 MockMVC 测试（CRUD + 异常路径 + 权限校验）
- 与已有 55 个 Service 测例分层互补
- **学完能答**："你们测试怎么分层的？Service 和 Controller 各测什么？"

### 5. Actuator + 日志聚合 🔧
- Spring Boot Actuator 健康检查 + 指标暴露
- Loki + Promtail 聚合 5 个微服务日志
- **学完能答**："微服务集群你怎么观测和维护的？"

### 6. Knife4j + 种子数据 + 全链路验证 🔧
- Knife4j API 文档配置 + 分组
- 种子数据预填充 SQL（演示用）
- 全链路启动验证接口完整性

### 7. Arthas 性能调优 📊
- 制造慢查询 / N+1 场景
- Arthas trace 定位慢方法
- 修复 + 调优报告
- 优先级最低，有余力再执行

---

## 📖 文档索引

| 文档 | 说明 |
|------|------|
| [README.md](README.md) | 项目首页（技术栈 / 架构 / 快速启动 / 亮点） |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 微服务架构设计 |
| [API_SPEC.md](API_SPEC.md) | REST API 详细规范 |
| [DATABASE.md](DATABASE.md) | 数据库设计（ER 图 + 表结构 + 索引 + 状态流转） |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志 |
