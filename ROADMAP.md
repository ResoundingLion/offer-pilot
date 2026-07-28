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
| Sprint 8 | 07-28 | offer-ai 智能助手（DeepSeek API + 前端聊天页面） |

## 🚧 后续路线

| Sprint | 内容 | 面试价值 |
|:------:|------|:--------:|
| Sprint 9 | Resume 简历管理：resume 表（支持多版本）+ CRUD API + 前端页面 | ⭐⭐⭐ |
| Sprint 10 | 测试补漏：Controller 层 MockMVC 测试（CRUD + 异常路径覆盖） | ⭐⭐⭐ |
| Sprint 11 | Knife4j + 全链路验证：API 文档配置 + 种子数据预填充 + 全链路测试 | ⭐⭐⭐ |
| Sprint 12 | Arthas 性能调优：制造慢查询/N+1 场景 → trace 定位 → 修复 + 调优报告 | ⭐⭐⭐⭐ |

### Sprint 9 — Resume 简历管理
- resume 表设计（支持多版本，`application_id` 关联投递）
- CRUD API + 设置默认简历
- 前端简历页面（列表 + 编辑器）
- 学完能答："简历版本怎么管理的？"

### Sprint 10 — 测试补漏
- Controller 层 MockMVC 测试
- 每个 Controller 的 CRUD + 异常路径覆盖
- 学完能答："你们 Controller 层怎么测的？"

### Sprint 11 — Knife4j + 全链路验证
- Knife4j 配置 + API 文档分组（加依赖、配置类、网关放行）
- 种子数据预填充 SQL
- 全链路测试：启动所有服务验证接口完整性
- 学完能答："你们项目 API 文档怎么管理的？"

### Sprint 12 — Arthas 性能调优
- 制造慢查询 / N+1 场景（故意写一条无索引查询或循环查 DB）
- 启动 Arthas attach 到运行中的服务
- 用 trace 命令定位慢方法
- 分析 SQL + 修复
- 写调优报告放 README
- 学完能答："线上性能问题怎么排查？"

---

## 📖 文档索引

| 文档 | 说明 |
|------|------|
| [README.md](README.md) | 项目首页（技术栈 / 架构 / 快速启动 / 亮点） |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 微服务架构设计 |
| [API_SPEC.md](API_SPEC.md) | REST API 详细规范 |
| [DATABASE.md](DATABASE.md) | 数据库设计（ER 图 + 表结构 + 索引 + 状态流转） |
| [CHANGELOG.md](CHANGELOG.md) | 更新日志 |
