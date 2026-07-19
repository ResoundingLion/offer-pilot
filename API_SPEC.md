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
