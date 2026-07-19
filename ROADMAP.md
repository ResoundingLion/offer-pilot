# OfferPilot 开发路线图

## 总体排期（2026年7月-9月）

目标：秋招前交付可演示的 MVP。

```
第1周 ──── 第2周 ──── 第3周 ──── 第4周 ──── 第5周 ──── 第6周 ──── 第7周
│          │          │          │          │          │          │
├─ Sprint 1 ─┤  ├─ Sprint 2 ─┤  ├─ Sprint 3 ─┤  ├─ Sprint 4 ─┤  ├─ Sprint 5
  脚手架搭建    用户+公司+      投递+面试+     前端全栈+     单元测试+
                岗位 CRUD      Offer 管理      Pipeline+      CI/CD
                                              一键推进
```

---

## Sprint 1（完成）—— 脚手架与基础设施

- ✅ 父 POM（Spring Boot 3.2.12 + Spring Cloud Alibaba 2023.0.3.4）
- ✅ Docker Compose：MySQL/Redis/Nacos
- ✅ Nacos 配置中心 + SQL 建表脚本
- ✅ offer-common：Result、异常处理、自动填充
- ✅ offer-gateway：路由 + 跨域 + JWT 全局过滤器
- ✅ offer-auth：注册/登录 + JWT 签发

---

## Sprint 2（完成）—— 用户 + 公司 + 岗位

- ✅ User CRUD（5 接口）
- ✅ Company CRUD（5 接口）
- ✅ Position CRUD（5 接口）
- ✅ 内部接口：Feign 跨服务调用支持

---

## Sprint 3（完成）—— 投递 + 面试 + Offer

- ✅ 6 枚举 + 3 Entity + 3 Mapper + 3 Service
- ✅ Application 6 接口（含状态机）
- ✅ Interview 4 接口 + Offer 4 接口
- ✅ Dashboard 统计接口
- ✅ Feign 跨服务调用（companyName/positionTitle）

---

## Sprint 4（完成）—— 前端全栈 + Pipeline 流水线

- ✅ Vue 3 七页前端全部完成
- ✅ 深色科技风 UI（扫描线、粒子登录、赛博卡片、页面转场）
- ✅ Pipeline 投递进度流水线（5 态阶段灯）
- ✅ 一键推进（动态弹窗 + 面试/Offer 一次创建）
- ✅ 流程配置（新增投递可选测评/笔试/多轮面试）
- ✅ Offer 占位（不填薪资不改状态）

---

## Sprint 5（待开始）—— 单元测试 + CI/CD

| 任务 | 说明 |
|------|------|
| 5.1 Service 层测试 | Mock Mapper + Mock Feign |
| 5.2 Controller 层测试 | MockMVC |
| 5.3 状态机流转测试 | 合法/非法路径全覆盖 |
| 5.4 GitHub Actions CI | push 自动编译 + 跑测试 |
| 5.5 JaCoCo 覆盖率 | 报告 + README badge |

---

## 后续（秋招后/有余力）

| 模块 | 说明 |
|------|------|
| Redis 缓存 | 缓存 Feign 结果、Cache-Aside、防雪崩穿透 |
| Sentinel 熔断 | Feign 降级 + 限流 |
| Docker 容器化 | 4 个服务打镜像 + 一键启动 |
| Knife4j | API 文档自动生成 |
| Arthas 调优 | 慢查询定位 + 修复 |
| 其他 | 面试提醒、简历管理、AI 分析 |
