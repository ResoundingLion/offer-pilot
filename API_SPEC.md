# OfferPilot REST API 规范

## 通用约定

- 基础路径：`/api/{资源名}`
- 请求/响应格式：JSON
- 分页参数：`page`（从1开始） / `size`（默认15）
- 统一响应包装：`Result<T>`

**统一响应格式：**

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1700000000000
}
```

**错误响应：**

| code | message | 说明 |
|------|---------|------|
| 401 | Unauthorized | Token 缺失或过期 |
| 403 | Forbidden | 无权限 |
| 404 | Not Found | 资源不存在 |
| 400 | Bad Request | 参数校验失败 |
| 500 | Internal Server Error | 服务器异常 |

业务错误码使用 `1xxx` 格式，由各服务自定义。

---

## Auth API (offer-auth)

### POST /api/auth/register

注册账号。

**Request：**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response：**
```json
{
  "token": "jwt_token_string",
  "userId": 1,
  "username": "demo"
}
```

### POST /api/auth/login

登录获取 Token。

**Request：**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response：** 同注册响应。

---

## User API (offer-user)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users/me | 获取当前用户资料（自动创建兜底） |
| PUT | /api/users/me | 更新当前用户资料（昵称/邮箱/手机/头像） |
| GET | /api/users | 用户列表 |
| GET | /api/users/{id} | 指定用户详情 |
| POST | /api/users | 新增用户 |
| PUT | /api/users | 更新用户 |
| DELETE | /api/users/{id} | 删除用户 |

### GET /api/users/me

自动创建兜底：如果 user 记录不存在（老用户未初始化），自动创建并绑定默认头像。

**Request Headers：**

| Header | 说明 |
|--------|------|
| X-User-Id | 用户 ID（网关鉴权后注入） |
| X-Username | 用户名（可选，自动创建时使用） |

**Response：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "demo",
    "email": "demo@example.com",
    "phone": "138xxxx",
    "avatar": "avatar/1/uuid.jpg",
    "avatarUrl": "http://localhost:9000/offer-pilot/avatar/1/uuid.jpg?X-Amz-Signature=..."
  },
  "message": "success"
}
```

### PUT /api/users/me

**Request：**
```json
{
  "name": "新名字",
  "email": "new@example.com",
  "phone": "13900112233",
  "avatar": "avatar/1/uuid.jpg"
}
```

> 只更新非空字段，传 null 的字段保持不变。

---

## Company API (offer-user)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/companies | 公司列表 |
| GET | /api/companies/{id} | 公司详情 |
| POST | /api/companies | 新增公司 |
| PUT | /api/companies/{id} | 更新公司 |
| DELETE | /api/companies/{id} | 删除公司 |

**GET /api/companies 查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | String | 公司名称搜索 |

**POST /api/companies Request：**
```json
{
  "name": "字节跳动",
  "industry": "互联网",
  "website": "https://bytedance.com",
  "location": "北京",
  "size": "10000+",
  "description": "..."
}
```

---

## Position API (offer-user)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/positions | 岗位列表（支持搜索） |
| GET | /api/positions/{id} | 岗位详情 |
| POST | /api/positions | 新增岗位 |
| PUT | /api/positions/{id} | 更新岗位 |
| DELETE | /api/positions/{id} | 删除岗位 |

---

## Application API (offer-application)

### 基础 CRUD

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/applications | 投递列表（支持筛选） |
| GET | /api/applications/{id} | 投递详情 |
| POST | /api/applications | 新增投递 |
| PUT | /api/applications/{id} | 更新投递 |
| PATCH | /api/applications/{id}/status | 更新状态 |
| DELETE | /api/applications/{id} | 删除投递 |

**GET /api/applications 查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | String | 公司/岗位搜索 |
| status | String | 按状态筛选 |
| source | String | 按渠道筛选 |

**POST /api/applications Request：**
```json
{
  "userId": 1,
  "companyId": 5,
  "positionId": 10,
  "source": "BOSS_ZHIPIN",
  "notes": "内推",
  "pipelineConfig": "ASSESSMENT,EXAM,INTERVIEW_3"
}
```

> `pipelineConfig` 为可选阶段配置，逗号分隔。不传则只显示固定阶段（投递→一面→HR面→Offer）。

### PATCH /api/applications/{id}/status

状态流转操作。

**Request：**
```json
{
  "status": "INTERVIEW"
}
```

**Response：** 返回更新后的投递记录（含 companyName/positionTitle）。

> 为什么用 PATCH 而不是 PUT：状态更新是部分字段变更，语义上 PATCH 更准确。

### PATCH /api/applications/{id}/advance

一键推进投递到下一阶段。一个请求同时处理状态变更 + 面试/Offer 记录创建。

**Request（推进到面试轮次）：**
```json
{
  "targetStage": "INTERVIEW_1",
  "interviewRound": "FIRST",
  "interviewScheduledAt": "2026-07-20T14:00:00",
  "interviewType": "ONLINE",
  "interviewInterviewer": "张工",
  "interviewResult": "PASSED",
  "interviewFeedback": "算法不错"
}
```

**Request（推进到 Offer）：**
```json
{
  "targetStage": "OFFER",
  "offerSalary": "25k × 15薪",
  "offerBonus": "签字费 5w",
  "offerStock": "1000 RSU",
  "offerBenefits": "餐补 + 补充医疗",
  "offerDeadline": "2026-08-01"
}
```

**Request（推进到简单阶段）：**
```json
{
  "targetStage": "ASSESSMENT"
}
```

**特殊规则：**
- 推进到 OFFER 但不填薪资 → 只记录 `current_stage`，不改 `application.status`（占位）
- 推进到已有面试记录的轮次 → 更新已有面试（补结果场景）
- 面试结果 FAILED → 自动将投递设为 REJECTED

---

## Dashboard API (offer-application)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/applications/dashboard/stats | 统计卡片 + 趋势图 + 渠道分布 |
| GET | /api/applications/dashboard/pipeline | 投递进度流水线 |

### GET /api/applications/dashboard/stats

**Response：**
```json
{
  "totalApplications": 50,
  "interviewCount": 8,
  "offerCount": 2,
  "activeCount": 30,
  "dailyTrend": [
    { "date": "2026-07-06", "count": 3 },
    { "date": "2026-07-07", "count": 1 }
  ],
  "sourceDistribution": [
    { "source": "BOSS_ZHIPIN", "count": 20 }
  ]
}
```

### GET /api/applications/dashboard/pipeline

返回所有投递的 Pipeline 阶段灯数据（按更新时间倒序）。

**Response：**
```json
[
  {
    "applicationId": 1,
    "companyName": "字节跳动",
    "positionTitle": "后端开发",
    "updatedAt": "2026-07-19T12:00:00",
    "stages": [
      { "stage": "APPLIED", "label": "已投递", "status": "COMPLETED" },
      { "stage": "ASSESSMENT", "label": "测评", "status": "COMPLETED" },
      { "stage": "EXAM", "label": "笔试", "status": "COMPLETED" },
      { "stage": "INTERVIEW_1", "label": "一面", "status": "ACTIVE" },
      { "stage": "INTERVIEW_2", "label": "二面", "status": "PENDING" },
      { "stage": "HR_INTERVIEW", "label": "HR面", "status": "PENDING" },
      { "stage": "OFFER", "label": "Offer", "status": "PENDING" }
    ]
  }
]
```

**阶段状态取值：**

| status | 含义 | 颜色 |
|--------|------|------|
| COMPLETED | 已完成 | 🟢 绿色 |
| ACTIVE | 进行中 | 🔵 青色（脉冲动画） |
| PENDING | 待进行 | ⚪ 暗色 |
| FAILED | 未通过 | 🔴 红色 ✕ |
| WITHDRAWN | 已放弃 | ⚫ 灰色 ✕ |

---

## Interview API (offer-application)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/applications/{appId}/interviews | 面试列表 |
| POST | /api/applications/{appId}/interviews | 新增面试 |
| PUT | /api/interviews/{id} | 更新面试 |
| DELETE | /api/interviews/{id} | 删除面试 |

---

## Offer API (offer-application)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/applications/{appId}/offers | Offer 详情（一个投递最多一个） |
| POST | /api/applications/{appId}/offers | 新增 Offer |
| PUT | /api/offers/{id} | 更新 Offer |
| PATCH | /api/offers/{id}/status | 接受/拒绝 Offer |

---

## Application 返回结构

### 完整 ApplicationVO

```json
{
  "id": 1,
  "userId": 1,
  "companyId": 5,
  "companyName": "字节跳动",
  "positionId": 10,
  "positionTitle": "后端开发",
  "status": "INTERVIEW",
  "source": "BOSS_ZHIPIN",
  "appliedAt": "2026-07-01T10:00:00",
  "notes": "内推",
  "pipelineConfig": "ASSESSMENT,EXAM,INTERVIEW_3",
  "currentStage": "INTERVIEW_1",
  "createdAt": "2026-07-01T10:00:00",
  "updatedAt": "2026-07-19T12:00:00"
}
```

> `companyName` 和 `positionTitle` 由 Feign 跨服务调用 offer-user 填充，失败时返回 null 不影响主流程。

---

## File API (offer-user / MinIO)

文件上传/下载服务，由 offer-user 模块提供，MinIO 对象存储在后端。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/files/upload | 上传文件（头像/附件） |
| GET | /api/files/download | 下载/预览文件 |

> 文件展示（如头像）直接使用预签名 URL 直连 MinIO，不走网关，减少网关压力。

### POST /api/files/upload

上传文件到 MinIO，返回对象路径和预签名访问 URL。

**Request：** `multipart/form-data`

| 参数 | 类型 | 说明 |
|------|------|------|
| file | MultipartFile | 上传的文件 |

**Request Headers：**

| Header | 说明 |
|--------|------|
| X-User-Id | 用户 ID（用于隔离路径: `avatar/{userId}/{uuid}.ext`） |

**Response：**
```json
{
  "code": 200,
  "data": {
    "objectName": "avatar/1/a1b2c3d4.jpg",
    "url": "http://localhost:9000/offer-pilot/avatar/1/a1b2c3d4.jpg?X-Amz-Signature=..."
  },
  "message": "success"
}
```

**错误响应：**

| code | message | 说明 |
|------|---------|------|
| 400 | 文件不能为空 | 上传空文件 |

### GET /api/files/download

根据 objectName 直接下载文件流。

**Query 参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| path | String | upload 返回的 objectName |

**响应：** 文件二进制流，Content-Type 根据扩展名自动推断。

---

## AI API (offer-ai)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/ai/chat | 旧版简单 AI 对话（保留兼容） |
| **POST** | **/api/ai/agent** | **Agent 模式（ReAct 多轮 Tool Calling，统一入口）** |
| **GET** | **/api/ai/conversations** | **获取当前用户对话列表** |
| **GET** | **/api/ai/conversations/{id}/messages** | **获取对话消息历史** |
| **DELETE** | **/api/ai/conversations/{id}** | **删除某个对话** |
| GET | /api/ai/test | 服务存活检测（白名单，无需 Token） |

### POST /api/ai/chat（旧版）

调用 LLM API 进行简单 AI 对话（无工具调用）。

**Request：**
```json
{
  "message": "帮我分析这个 JD：精通 Java、Spring Cloud"
}
```

**Response：**
```json
{
  "code": 200,
  "data": "Spring Cloud是一套基于Spring Boot的微服务解决方案...",
  "message": "success"
}
```

### POST /api/ai/agent（推荐）

**Agent 入口** —— LLM 自主决策调用哪些工具获取数据，支持多轮 ReAct 循环。

**Request：**
```json
{
  "message": "看看我的求职投递情况，分析拒信原因和优化方向",
  "conversationId": null
}
```

> `conversationId` 为可选项。新对话不传或传 null；续聊时传上一次返回的 conversationId。

**Response：**
```json
{
  "code": 200,
  "data": {
    "conversationId": 1,
    "reply": "## 投递分析\n\n### 总览\n..."
  },
  "message": "success"
}
```

> reply 为 Markdown 格式，前端渲染为结构化文本。

**Agent 工作流程：**
```
用户："看看我的求职情况"
  → 第1轮：LLM 决定调 get_dashboard_stats → 拿到统计数据
  → 第2轮：LLM 发现拒信多 → 调 get_applications → 拿到投递列表
  → 第3轮：LLM 看到面试挂的多 → 调 get_interviews → 拿到面试反馈
  → 最终：LLM 综合分析 → 返回结构化回复
```

**可用的工具（LLM 自主选择）：**

| 工具名 | 获取数据 | 来源 |
|--------|---------|------|
| `get_active_resume` | 当前简历内容（含 contentText） | offer-user（Feign） |
| `get_dashboard_stats` | 投递统计总览 | offer-application（Feign） |
| `get_applications` | 全部投递记录列表 | offer-application（Feign） |
| `get_interviews` | 某投递的面试记录 | offer-application（Feign） |
| `get_offer` | 某投递的 Offer | offer-application（Feign） |

### GET /api/ai/conversations

获取当前用户的历史对话列表（按更新时间倒序）。

**请求头：** `X-User-Id`（网关自动注入）

**Response：**
```json
{
  "code": 200,
  "data": [
    { "id": 1, "userId": 1, "title": "看看我的求职投递情况...", "createdAt": "...", "updatedAt": "..." }
  ],
  "message": "success"
}
```

### GET /api/ai/conversations/{id}/messages

获取指定对话的全部消息（按序号排列，用于前端加载历史）。

**Response：**
```json
{
  "code": 200,
  "data": [
    { "id": 1, "role": "USER", "content": "看看我的求职情况", "msgIndex": 1 },
    { "id": 2, "role": "TOOL_USE", "toolName": "get_dashboard_stats", "msgIndex": 2 },
    { "id": 3, "role": "TOOL_RESULT", "toolResult": "{\"total\":20,...}", "msgIndex": 3 },
    { "id": 4, "role": "ASSISTANT", "content": "## 分析结果...", "msgIndex": 4 }
  ],
  "message": "success"
}
```

**错误响应：**
- 400: 消息为空（agent 端）
- 403: 对话不属于当前用户
- 404: 对话不存在
- 500: AI API 调用失败（网络/Key 过期等，返回友好提示）

> AI API Key 配置在 Nacos 配置中心（`offer-ai.yaml`），可切换不同模型或 API 提供商。
> 使用 Anthropic 兼容接口，通过配置切换不同 LLM 提供商。

---

## Resume API (offer-user)

简历管理，支持多版本（同一 `title` 下 `version` 递增）。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/resumes | 当前用户全部简历 |
| GET | /api/resumes/{id} | 简历详情 |
| POST | /api/resumes | 创建新简历（自动算版本号） |
| PUT | /api/resumes/{id} | 更新简历内容 |
| DELETE | /api/resumes/{id} | 删除某个版本 |
| PATCH | /api/resumes/{id}/new-version | 基于当前版本创建新版本 |
| PATCH | /api/resumes/{id}/current | 设为当前使用版本 |
| POST | /api/resumes/{id}/extract-text | 从 PDF 提取文本到 content_text |

### POST /api/resumes

**Request：**
```json
{
  "title": "Java后端简历",
  "content": "skills: Java, Spring, MySQL",
  "fileUrl": "resume/1/uuid.pdf"
}
```

> `userId` 由网关从 JWT 解析后通过 `X-User-Id` 请求头注入，前端不需要传。`fileUrl` 可选，通过先上传文件到 MinIO 拿到 objectName 再填入。

**Response：**
```json
{
  "id": 1,
  "userId": 1,
  "title": "Java后端简历",
  "version": 1,
  "content": "skills: Java, Spring, MySQL",
  "fileUrl": "resume/1/uuid.pdf",
  "summary": null,
  "isCurrent": true,
  "createdAt": "2026-07-29T10:21:11",
  "updatedAt": "2026-07-29T10:21:11"
}
```

### PUT /api/resumes/{id}

只更新 `content`、`fileUrl`、`summary` 三个字段，`title` 和 `version` 不可修改。

**Request：**
```json
{
  "content": "updated content"
}
```

### PATCH /api/resumes/{id}/new-version

基于 id 对应版本的 `content`、`fileUrl`、`summary` 复制并创建新版本，版本号自动 +1。

### PATCH /api/resumes/{id}/current

将指定版本设为当前使用版本。同一用户同 title 下其他版本的 `isCurrent` 自动置为 `false`。

### POST /api/resumes/{id}/extract-text

从 MinIO 下载简历关联的 PDF 文件 → PDFBox 提取纯文本 → 写入 `content_text` 字段。

适用于创建弹窗流程（上传时无 resumeId 自动提取），或手动触发重新提取。

**请求参数：** 无请求体
**响应：** `Result<Void>`
**前置条件：** resume 必须有 file_url 且以 .pdf 结尾
**异常处理：** 提取失败返回 500，不阻塞后续操作

> **多版本设计说明：** 同 title = 同一份简历的不同版本（如 "Java后端简历 v1"、"Java后端简历 v2"）。用 `title` 而非 `group_id` 分组更直观，减少用户理解成本。

---

## Internal API（Feign 内部接口）

服务间跨服务调用使用的内部接口，不经过网关，不走 `Result` 包装，直接返回 DTO。

### Company Internal

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/companies/{id} | 获取公司 DTO（Feign 调用） |

**Response：** 直接返回 `CompanyDTO`（非 `Result` 包装）：
```json
{
  "id": 5,
  "name": "字节跳动",
  "industry": "互联网",
  "website": "https://bytedance.com",
  "location": "北京"
}
```

> 找不到时返回空 DTO（所有字段为 null），避免 Feign 反序列化异常。

### Position Internal

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/positions/{id} | 获取岗位 DTO（Feign 调用） |

**Response：** 直接返回 `PositionDTO`：
```json
{
  "id": 10,
  "companyId": 5,
  "title": "后端开发工程师",
  "city": "北京",
  "salaryMin": 25,
  "salaryMax": 50
}
```

### User Internal

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /internal/users/{id} | 获取用户 DTO（Feign 调用） |

**Response：** 直接返回 `UserDTO`。

### Resume Internal（2026-07-30 新增，供 AI Agent 调用）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/resumes/active | 获取用户当前使用的简历（含 contentText） |

**Query：** `userId`

**Response：** 直接返回 `ResumeDTO`（含 contentText 供 AI 做简历匹配分析）。

### Application Internal（2026-07-30 新增，供 AI Agent 调用）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/applications | 获取用户全部投递记录（含状态/渠道/日期） |
| GET | /internal/applications/dashboard/stats | 获取投递统计数据（趋势/来源分布） |

**Query：** `userId`

### Interview Internal（2026-07-30 新增，供 AI Agent 调用）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/interviews | 获取某投递的面试记录列表 |

**Query：** `applicationId`

### Offer Internal（2026-07-30 新增，供 AI Agent 调用）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /internal/offers | 获取某投递的 Offer 详情 |

**Query：** `applicationId`

> Internal API 的 Feign 接口定义在 `offer-api` 模块。原有的 `CompanyClient` / `PositionClient` / `UserClient` 供 offer-application 调用。2026-07-30 新增 `ResumeClient` / `ApplicationClient` / `InterviewClient` / `OfferClient` 供 offer-ai Agent 调用。所有 Feign 调用均配置 FallbackFactory 熔断降级。

---

## 接口命名规范

- 资源用复数名词：`/api/users` 而非 `/api/user`
- 嵌套资源用路径表达：`/api/applications/{id}/interviews`
- 操作用 HTTP 方法表达，不要出现在 URL 里：
  - ✅ `PATCH /api/applications/{id}/status`
  - ❌ `/api/applications/updateStatus`
- 筛选/排序用 Query 参数：`?status=INTERVIEW&page=1&size=15`

### 要点说明

为什么 Interview 和 Offer 用嵌套路径？因为它们从属于 Application，删除一个投递面试和 Offer 应该一起删。用嵌套路径比平铺 `/api/interviews` 更能体现业务归属关系，前端也更容易理解。

为什么 `advance` 放在 Application 而非 Dashboard 路径？`advance` 操作的是单个投递的状态推进，业务上属于 Application 资源的行为，用 PATCH。
