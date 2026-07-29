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
| **Sprint 9** | **07-29** | **Resume 简历管理（多版本）+ PDF 文本提取 — 后端 8 接口 + 前端 8 页面 + 选文件即上传 + PDF 预览 + 文本提取** |

## 🚧 后续路线（2026-07-29 修订）

基于项目评价+面试价值+实际使用需求重新排序，共 7 站。

| 序 | 任务 | 面试价值 | 状态 |
|:--:|------|:--------:|:----:|
| 1️⃣ | **Resume 简历管理（多版本）** | 补领域模型，用户自用 | **✅ 已完成** |
| 2️⃣ | **AI Agent 升级（JD分析/投递分析/面试助手）** | 🔥 最大差异化 | 待开始 |
| 3️⃣ | **Notification 通知服务** | 圆 RabbitMQ 的故事 | 待开始 |
| 4️⃣ | **Controller 测试补漏** | 测试分层覆盖 | 待开始 |
| 5️⃣ | **Actuator + Loki 日志聚合** | 微服务运维故事 | 待开始 |
| 6️⃣ | **Knife4j + 种子数据 + 全链路验证** | 收尾展示 | 待开始 |
| 7️⃣ | **Arthas 性能调优** | 差异化亮点，有余力再执行 | 待开始 |

### 1. Resume 简历管理 📄  ✅ 已完成（2026-07-29）

> 全栈实现。后端 8 接口 + 前端简历管理页 + PDF 文本提取供 AI 分析。
> 详见 [CHANGELOG.md](CHANGELOG.md) 1.5.0-SNAPSHOT。

- resume 表设计（多版本：同一 title 下 version 递增，`user_id + title + version` 联合唯一）
- CRUD + 创建新版本 + 切换当前使用版本
- 内容存储结构化 JSON，file_url（PDF/Word 上传 MinIO）
- PDF 文本提取：Apache PDFBox 提取简历纯文本 → `content_text` 字段（16MB 上限）
- 两种提取方式：上传时自动提取（带 resumeId） + 手动接口 `POST /{id}/extract-text`
- **学完能答**："LLM 不是多模态，怎么分析简历 PDF？"

### 2. AI Agent 升级 🤖
从"聊天 API 代理"升级为真正的求职 Agent：
- **JD 智能分析**：粘贴 JD → 关键词 + 难度 + 技能匹配度 + 学习建议
- **AI 面试助手**：公司+岗位 → 高频面试题 + 八股重点 + 项目追问（需 Tool Calling）
- **AI 投递分析**：扫描投递数据 → 拒信原因分析 + 优化方向（需 Resume 完成）
- 关键技术：Tool Calling + 数据库查询 + 结构化输出
- **学完能答**："怎么把 AI 嵌入业务？Tool Calling 怎么设计的？"

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
