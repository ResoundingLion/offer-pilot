# OfferPilot 开发路线图（2026-07-27 全面修订）

目标：秋招前交付可演示的完整项目，全部规划内功能一个不漏。

---

## ✅ 已完成的 Sprint

### Sprint 1-4（2026-07-13 ~ 2026-07-19）—— 基础功能 + 前端全栈

| Sprint | 内容 | 状态 |
|--------|------|------|
| Sprint 1 | 脚手架搭建（父 POM、Docker、Nacos、Gateway、Auth）| ✅ |
| Sprint 2 | 用户 + 公司 + 岗位 CRUD（15 接口）| ✅ |
| Sprint 3 | 投递 + 面试 + Offer + 状态机（15 接口）| ✅ |
| Sprint 4 | Vue 3 前端 7 页全功能 + Pipeline + 一键推进 | ✅ |

### 工程化增强（2026-07-23 ~ 2026-07-24）

| 站 | 内容 | 状态 |
|----|------|------|
| 第一站 | 单元测试（55 测例全绿）| ✅ |
| 第二站 | GitHub Actions CI + JaCoCo | ✅ |
| 第三站 | Redis 缓存（Cache-Aside + 防雪崩/穿透）| ✅ |
| 第四站 | Sentinel 熔断降级（Feign + FallbackFactory）| ✅ |

### 基础设施补齐（2026-07-27 ~ 2026-07-28）

| 站 | 内容 | 状态 |
|----|------|------|
| 第一阶段 | MinIO 文件存储（上传/下载 API + 头像 OSS）| ✅ |
| 第二阶段 | RabbitMQ 消息队列（Topic Exchange + 状态变更事件）| ✅ |

---

## 🚧 后续路线（五阶段）

```
第一阶段：智能助手 offer-ai 🤖      ← DeepSeek API 集成（MinIO+RabbitMQ 已完成）
第二阶段：Resume 简历管理 📄        ← 补领域模型
第三阶段：测试补漏 🧪               ← Controller 层测试
第四阶段：Knife4j + 全链路验证 🔧   ← API 文档 + 种子数据
第五阶段：Arthas 性能调优 📊        ← 差异化亮点
```

### 第一阶段：智能助手 offer-ai 🤖
- 新建 offer-ai 微服务（注册 Nacos）
- DeepSeek API 集成（Key 配在 application.yml 可手动改）
- JD 分析 + 面试题生成
- 前端 AI 助手页面
- 面试价值：⭐⭐⭐⭐⭐ 差异化亮点

### 第二阶段：Resume 简历管理
- resume 表（支持多版本）+ CRUD API
- 前端简历页面
- 面试价值：⭐⭐⭐ 功能完整性

### 第三阶段：测试补漏
- Controller 层 MockMVC 测试（CRUD + 异常路径覆盖）
- 面试价值：⭐⭐⭐

### 第四阶段：Knife4j + 全链路验证 🔧
- Knife4j API 文档配置 + 网关放行
- 种子数据预填充 SQL
- 全链路测试验证
- 面试价值：⭐⭐⭐ 收尾

### 第五阶段：Arthas 性能调优 📊
- 制造慢查询 / N+1 场景
- Arthas trace 定位慢方法
- 修复 + 写调优报告放 README
- 面试价值：⭐⭐⭐⭐ 应届生差异化亮点
