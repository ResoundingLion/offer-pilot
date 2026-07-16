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

**分页响应格式：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 15
  }
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

### POST /api/auth/login

登录获取 Token。

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
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 7200
}
```

### POST /api/auth/refresh

刷新 Token（使用 refresh_token）。

**Request：**
```json
{
  "refreshToken": "string"
}
```

**Response：** 同登录响应。

### POST /api/auth/logout

退出登录，使 Token 失效。

**Header：** `Authorization: Bearer {token}`

---

## User API (offer-user)

### GET /api/users/me

获取当前用户信息。

**Response：**
```json
{
  "id": 1,
  "email": "string",
  "phone": "string",
  "name": "string",
  "avatar": "string"
}
```

### PUT /api/users/me

更新个人资料。

### POST /api/users/register

注册。

---

## Company API (offer-user)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/companies | 公司列表（分页） |
| GET | /api/companies/{id} | 公司详情 |
| POST | /api/companies | 新增公司 |
| PUT | /api/companies/{id} | 更新公司 |
| DELETE | /api/companies/{id} | 删除公司 |

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
| GET | /api/positions | 岗位列表（分页，支持按公司/城市/类型筛选） |
| GET | /api/positions/{id} | 岗位详情 |
| POST | /api/positions | 新增岗位 |
| PUT | /api/positions/{id} | 更新岗位 |
| DELETE | /api/positions/{id} | 删除岗位 |

**GET /api/positions 查询参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| companyId | Long | 按公司筛选 |
| city | String | 按城市筛选 |
| employmentType | String | 按类型筛选 |
| keyword | String | 关键词搜索 |
| page | Integer | 页码 |
| size | Integer | 每页条数 |

---

## Application API (offer-application)

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/applications | 投递列表（分页，支持按状态/公司/日期筛选） |
| GET | /api/applications/{id} | 投递详情 |
| POST | /api/applications | 新增投递 |
| PUT | /api/applications/{id} | 更新投递 |
| PATCH | /api/applications/{id}/status | 更新状态 |
| DELETE | /api/applications/{id} | 删除投递 |

### PATCH /api/applications/{id}/status

状态流转操作。

**Request：**
```json
{
  "status": "INTERVIEW"
}
```

**Response：** 返回更新后的投递记录。

> 为什么用 PATCH 而不是 PUT：状态更新是部分字段变更，语义上 PATCH 更准确。

### GET /api/applications/stats

Dashboard 统计。

**Response：**
```json
{
  "totalApplications": 50,
  "byStatus": {
    "SAVED": 10,
    "APPLIED": 15,
    "INTERVIEW": 8,
    "OFFER": 2,
    "REJECTED": 12,
    "WITHDRAWN": 3
  },
  "interviewRate": "46.0%",
  "offerRate": "6.7%"
}
```

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
| GET | /api/applications/{appId}/offers | Offer 列表(通常一个投递只有一个) |
| POST | /api/applications/{appId}/offers | 新增 Offer |
| PUT | /api/offers/{id} | 更新 Offer |
| PATCH | /api/offers/{id}/status | 接受/拒绝 Offer |

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
