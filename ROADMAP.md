# OfferPilot 开发路线图

目标：秋招前交付可演示的完整项目，全部规划内功能一个不漏。

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

## 🚧 后续路线（2026-07-30 修订）

基于项目评价+面试价值+实际使用需求重新排序，共 6 站。

| 序 | 任务 | 面试价值 | 状态 |
|:--:|------|:--------:|:----:|
| 1️⃣ | Resume 简历管理（多版本） | 补领域模型，用户自用 | **✅ 已完成** |
| 2️⃣ | **AI Agent 升级（JD分析/投递分析/面试助手）** | 🔥 最大差异化 | **✅ 已完成** |
| 3️⃣ | **Notification 通知服务** | 圆 RabbitMQ 的故事 | 待开始 |
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

### 3. Notification 通知服务 📨
- 新建 offer-notification 微服务（端口 8085）
- 站内信 + 邮件通知
- 状态变更 → MQ TopicExchange → 通知消费者 → 发送通知
- **学完能答**："MQ 在你们项目里到底解决了什么实际问题？"

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
