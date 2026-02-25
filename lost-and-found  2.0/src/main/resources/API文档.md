# 校园失物招领平台 API 文档

## 1. 概述

本文档描述了校园失物招领平台的后端API接口，包括认证、用户管理、失物管理、招领管理、评论管理和活动日志管理等模块。所有接口均以RESTful风格设计，使用JSON格式进行数据传输。

## 2. API 基础信息

### 2.1 接口前缀
所有接口均以 `/api` 为前缀，例如：`/api/auth/login`

### 2.2 响应格式

**成功响应**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    // 响应数据，根据接口不同返回不同格式
  }
}
```

**失败响应**：
```json
{
  "code": 错误码,
  "message": "错误信息"
}
```

### 2.3 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未登录或登录过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 2.4 认证方式

- **认证类型**：Token 认证
- **认证头**：`Authorization: <token>`
- **Token 获取**：通过登录接口获取

## 3. 认证接口

### 3.1 用户登录
- **接口地址**：`/api/auth/login`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | accountName | string | 否 | 登录账号（优先匹配） |
  | username | string | 否 | 用户名（回退匹配） |
  | password | string | 是 | 密码 |
  | captchaId | string | 是 | 验证码ID |
  | captchaCode | string | 是 | 验证码内容 |
  | rememberMe | boolean | 否 | 是否签发7天 refresh token |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": "1",
      "username": "admin",
      "name": "管理员",
      "role": "ADMIN",
      "phone": "13800138000",
      "email": "admin@example.com"
    }
  }
}
```

### 3.2 用户注册
- **接口地址**：`/api/auth/register`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | username | string | 是 | 用户名 |
  | password | string | 是 | 密码 |
  | name | string | 是 | 真实姓名 |
  | phone | string | 是 | 手机号 |
  | email | string | 是 | 邮箱 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "注册成功",
  "data": null
}
```

### 3.3 用户登出
- **接口地址**：`/api/auth/logout`
- **请求方法**：POST
- **请求参数**：无
- **响应数据**：
```json
{
  "code": 0,
  "message": "登出成功",
  "data": null
}
```

### 3.4 获取用户信息
- **接口地址**：`/api/user/info`
- **请求方法**：GET
- **请求参数**：无
- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": "1",
    "username": "admin",
    "name": "管理员",
    "role": "ADMIN",
    "phone": "13800138000",
    "email": "admin@example.com"
  }
}
```

### 3.5 更新用户信息
- **接口地址**：`/api/user/update`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | username | string | 否 | 昵称（可修改，需唯一） |
  | phone | string | 否 | 手机号（可修改） |
  | email | string | 否 | 邮箱（可修改） |

- **字段可编辑性说明**：
  - 可编辑：`username`、`phone`、`email`
  - 不可编辑：`accountName`、`name`、`college`

- **响应数据**：
```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

### 3.6 重置密码
- **接口地址**：`/api/auth/resetPassword`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | oldPassword | string | 是 | 旧密码 |
  | newPassword | string | 是 | 新密码（8-20位，字母+数字） |

- **响应数据**：
```json
{
  "code": 0,
  "message": "密码重置成功",
  "data": null
}
```

## 4. 失物管理接口

### 4.1 发布失物信息
- **接口地址**：`/api/lost/publish`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | name | string | 是 | 物品名称 |
  | type | string | 是 | 物品类型 |
  | location | string | 是 | 丢失地点 |
  | description | string | 是 | 物品描述 |
  | contactPhone | string | 是 | 联系电话 |
  | contactEmail | string | 是 | 联系邮箱 |
  | images | array | 否 | 物品图片列表 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "发布成功",
  "data": null
}
```

### 4.2 获取失物列表
- **接口地址**：`/api/lost/list`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | page | number | 否 | 当前页码，默认1 |
  | pageSize | number | 否 | 每页条数，默认10 |
  | keyword | string | 否 | 搜索关键词 |
  | type | string | 否 | 物品类型 |
  | status | string | 否 | 物品状态 |
  | startTime | string | 否 | 发布开始时间 |
  | endTime | string | 否 | 发布结束时间 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "list": [
      {
        "id": "1",
        "name": "校园卡",
        "type": "ID_CARD",
        "location": "图书馆",
        "status": "APPROVED",
        "description": "在图书馆二楼丢失的校园卡，姓名：张三",
        "viewCount": 10,
        "publishTime": "2023-01-01T10:00:00.000Z"
      }
    ],
    "total": 1
  }
}
```

### 4.3 获取失物详情
- **接口地址**：`/api/lost/detail/:id`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | string | 是 | 失物ID |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "id": "1",
    "name": "校园卡",
    "type": "ID_CARD",
    "location": "图书馆",
    "status": "APPROVED",
    "description": "在图书馆二楼丢失的校园卡，姓名：张三",
    "contactPhone": "13800138000",
    "contactEmail": "zhangsan@example.com",
    "images": [],
    "viewCount": 10,
    "publishTime": "2023-01-01T10:00:00.000Z"
  }
}
```

### 4.4 更新失物状态
- **接口地址**：`/api/lost/updateStatus`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | string | 是 | 失物ID |
  | status | string | 是 | 新状态 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "状态更新成功",
  "data": null
}
```

### 4.5 获取用户发布的失物信息
- **接口地址**：`/api/lost/userList`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | page | number | 否 | 当前页码，默认1 |
  | pageSize | number | 否 | 每页条数，默认10 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "list": [
      {
        "id": "1",
        "name": "校园卡",
        "type": "ID_CARD",
        "location": "图书馆",
        "status": "APPROVED",
        "viewCount": 10,
        "publishTime": "2023-01-01T10:00:00.000Z"
      }
    ],
    "total": 1
  }
}
```

### 4.6 获取热门失物列表
- **接口地址**：`/api/lost/hot`
- **请求方法**：GET
- **请求参数**：无
- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": "1",
      "name": "校园卡",
      "type": "ID_CARD",
      "location": "图书馆",
      "status": "APPROVED",
      "viewCount": 10,
      "publishTime": "2023-01-01T10:00:00.000Z"
    }
  ]
}
```

## 5. 招领管理接口

### 5.1 发布招领信息
- **接口地址**：`/api/find/publish`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | name | string | 是 | 物品名称 |
  | type | string | 是 | 物品类型 |
  | location | string | 是 | 发现地点 |
  | description | string | 是 | 物品描述 |
  | contactPhone | string | 是 | 联系电话 |
  | contactEmail | string | 是 | 联系邮箱 |
  | images | array | 否 | 物品图片列表 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "发布成功",
  "data": null
}
```

### 5.2 获取招领列表
- **接口地址**：`/api/find/list`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | page | number | 否 | 当前页码，默认1 |
  | pageSize | number | 否 | 每页条数，默认10 |
  | keyword | string | 否 | 搜索关键词 |
  | type | string | 否 | 物品类型 |
  | status | string | 否 | 物品状态 |
  | startTime | string | 否 | 发布开始时间 |
  | endTime | string | 否 | 发布结束时间 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "list": [
      {
        "id": "1",
        "name": "钱包",
        "type": "OTHER",
        "location": "第一食堂",
        "status": "APPROVED",
        "description": "在食堂捡到的黑色钱包，内有现金和银行卡",
        "viewCount": 15,
        "publishTime": "2023-01-02T14:30:00.000Z"
      }
    ],
    "total": 1
  }
}
```

### 5.3 获取热门招领列表
- **接口地址**：`/api/find/hot`
- **请求方法**：GET
- **请求参数**：无
- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": [
    {
      "id": "1",
      "name": "钱包",
      "type": "OTHER",
      "location": "第一食堂",
      "status": "APPROVED",
      "viewCount": 15,
      "publishTime": "2023-01-02T14:30:00.000Z"
    }
  ]
}
```

## 6. 评论管理接口

### 6.1 发表评论
- **接口地址**：`/api/comment/add`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | infoId | string | 是 | 信息ID |
  | infoType | string | 是 | 信息类型（lost/find） |
  | content | string | 是 | 评论内容 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "评论成功",
  "data": null
}
```

### 6.2 获取评论列表
- **接口地址**：`/api/comment/list`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | infoId | string | 是 | 信息ID |
  | infoType | string | 是 | 信息类型（lost/find） |
  | page | number | 否 | 当前页码，默认1 |
  | pageSize | number | 否 | 每页条数，默认10 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "list": [
      {
        "id": "1",
        "infoId": "1",
        "infoType": "lost",
        "userId": "1",
        "username": "student1",
        "content": "我看到了这个校园卡，在图书馆二楼服务台",
        "likeCount": 2,
        "createTime": "2023-01-01T10:00:00.000Z"
      }
    ],
    "total": 1
  }
}
```

### 6.3 点赞评论
- **接口地址**：`/api/comment/like`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | commentId | string | 是 | 评论ID |

- **响应数据**：
```json
{
  "code": 0,
  "message": "点赞成功",
  "data": null
}
```

## 7. 管理员接口

### 7.1 获取待审核列表
- **接口地址**：`/api/audit/list`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | page | number | 否 | 当前页码，默认1 |
  | pageSize | number | 否 | 每页条数，默认10 |
  | type | string | 否 | 信息类型（lost/find/all） |
  | keyword | string | 否 | 搜索关键词 |
  | status | string | 否 | 审核状态（PENDING） |
  | startTime | number | 否 | 开始时间戳 |
  | endTime | number | 否 | 结束时间戳 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "list": [
      {
        "id": "1",
        "type": "lost",
        "name": "校园卡",
        "status": "PENDING",
        "publishTime": "2023-01-01T10:00:00.000Z"
      }
    ],
    "total": 1
  }
}
```

### 7.2 审核通过
- **接口地址**：`/api/audit/pass`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | string | 是 | 信息ID |
  | type | string | 是 | 信息类型（lost/find） |

- **响应数据**：
```json
{
  "code": 0,
  "message": "审核通过",
  "data": null
}
```

### 7.3 批量审核通过
- **接口地址**：`/api/audit/batchPass`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | idAndTypeList | array | 是 | ID和类型列表，格式：[{"id": "1", "type": "lost"}, ...] |

- **响应数据**：
```json
{
  "code": 0,
  "message": "批量审核通过",
  "data": null
}
```

### 7.4 审核拒绝
- **接口地址**：`/api/audit/reject`
- **请求方法**：POST
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | id | string | 是 | 信息ID |
  | type | string | 是 | 信息类型（lost/find） |
  | reason | string | 是 | 拒绝原因 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "审核拒绝",
  "data": null
}
```

### 7.5 获取用户列表
- **接口地址**：`/api/admin/users`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | page | number | 否 | 当前页码，默认1 |
  | pageSize | number | 否 | 每页条数，默认10 |
  | username | string | 否 | 用户名搜索关键词 |
  | role | string | 否 | 用户角色 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "list": [
      {
        "id": "1",
        "username": "admin",
        "name": "管理员",
        "role": "ADMIN",
        "phone": "13800138000",
        "email": "admin@example.com",
        "createTime": "2023-01-01T00:00:00.000Z"
      }
    ],
    "total": 1
  }
}
```

### 7.6 更新用户角色
- **接口地址**：`/api/admin/users/:id/role`
- **请求方法**：PUT
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | role | string | 是 | 新角色（STUDENT/TEACHER/ADMIN） |

- **响应数据**：
```json
{
  "code": 0,
  "message": "角色修改成功",
  "data": null
}
```

### 7.7 删除用户
- **接口地址**：`/api/admin/users/:id`
- **请求方法**：DELETE
- **请求参数**：无
- **响应数据**：
```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

## 5. 统计接口

### 5.1 获取统计信息
- **接口地址**：`/api/statistics`
- **请求方法**：GET
- **请求参数**：无
- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "lostCount": 100,
    "findCount": 50,
    "userCount": 1000,
    "commentCount": 200
  }
}
```

## 6. 活动日志接口

### 6.1 获取活动日志列表
- **接口地址**：`/api/admin/activities`
- **请求方法**：GET
- **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | page | number | 否 | 当前页码，默认1 |
  | pageSize | number | 否 | 每页条数，默认10 |
  | userId | string | 否 | 用户ID |
  | action | string | 否 | 操作类型 |
  | itemType | string | 否 | 物品类型 |

- **响应数据**：
```json
{
  "code": 0,
  "message": "",
  "data": {
    "list": [
      {
        "id": "1",
        "userId": "1",
        "action": "publish",
        "itemType": "lost",
        "itemId": "1",
        "content": "发布了失物信息：校园卡",
        "createTime": "2023-01-01T10:00:00.000Z"
      }
    ],
    "total": 1
  }
}
```

## 7. 认证与授权

### 7.1 Token 认证
- **登录成功后**：服务器返回 `token`，客户端需要将其存储在本地
- **请求头**：在后续请求中，将 `token` 添加到请求头的 `Authorization` 字段中
- **Token 失效**：当 `token` 过期或无效时，服务器返回 `code: 401`，客户端需要清除本地存储并跳转到登录页

### 7.2 权限控制
- **普通用户**：可以发布、查看、评论失物和招领信息
- **管理员**：除普通用户权限外，还可以审核信息、管理用户和查看活动日志

## 8. 数据格式要求

### 8.1 日期时间格式
- 所有日期时间字段均使用ISO 8601格式，例如：`2023-01-01T10:00:00.000Z`

### 8.2 分页格式
- 所有列表接口均需支持分页，返回格式统一为：
  ```json
  {
    "list": [...], // 数据列表
    "total": 100 // 总条数
  }
  ```

## 9. 安全要求

1. **密码加密**：用户密码需使用bcrypt等安全算法进行加密存储
2. **SQL注入防护**：所有数据库操作需使用参数化查询，防止SQL注入
3. **XSS防护**：对用户输入进行过滤和转义，防止跨站脚本攻击
4. **CSRF防护**：实现CSRF令牌机制，防止跨站请求伪造
5. **接口限流**：对敏感接口实施限流，防止恶意请求

## 10. 性能要求

1. **响应时间**：所有接口响应时间需控制在2秒以内
2. **并发处理**：系统需支持至少100个并发请求
3. **数据缓存**：对热点数据进行缓存，提高系统性能
4. **数据库优化**：合理设计数据库索引，优化查询性能

## 11. 开发规范

1. **接口命名**：使用小写字母和下划线分隔，例如：`/api/auth/login`
2. **HTTP方法**：
   - GET：获取资源
   - POST：创建资源
   - PUT：更新资源
   - DELETE：删除资源
3. **参数命名**：使用小驼峰命名，例如：`contactPhone`
4. **版本控制**：建议在接口前缀中添加版本号，例如：`/api/v1/auth/login`
5. **文档更新**：接口变更时，需及时更新文档

## 12. 2026-02-25 增量变更（以代码实现为准）

### 12.1 认证与账号模型

#### 登录接口（支持账号或用户名）
* **接口地址**：`/api/auth/login`
* **请求方法**：POST（`application/x-www-form-urlencoded`）
* **请求参数**：
  | 参数名 | 类型 | 必填 | 描述 |
  |--------|------|------|------|
  | accountName | string | 否 | 登录账号（优先） |
  | username | string | 否 | 用户名（回退匹配） |
  | password | string | 是 | 密码 |
  | captchaId | string | 是 | 验证码ID |
  | captchaCode | string | 是 | 验证码内容 |
  | rememberMe | boolean | 否 | 是否签发7天 refresh token |

#### 个人信息更新接口（字段可编辑性）
* **接口地址**：`/api/user/update`
* **请求方法**：POST
* **仅允许更新**：`username`、`phone`、`email`
* **禁止普通用户更新**：`accountName`、`name`、`college`

### 12.2 密码策略（端到端统一）
* 注册与改密统一规则：`8-20` 位，且必须同时包含字母和数字。
* 后端强校验入口：
  * `POST /api/auth/register`
  * `POST /api/auth/resetPassword`
* `resetPassword` 同时兼容 `RequestParam` 与 JSON Body 传参。

### 12.3 删除与恢复策略
* 下列表采用逻辑删除并默认过滤：
  * `activities`
  * `comment`
  * `comment_like`
  * `honor_period`
  * `honor_period_item`
* `operation_log` 保持物理日志策略（无删除接口）。

#### 新增恢复接口
* `PUT /api/admin/comments/{id}/recover`
* `PUT /api/admin/comments/batch/recover`
* `PUT /api/admin/activities/{id}/recover`
* `PUT /api/admin/activities/batch/recover`
* `PUT /api/admin/honor/items/{id}/recover`
* `PUT /api/admin/honor/periods/{periodId}/recover`

### 12.4 数据迁移脚本
* 新增脚本：`update_schema_v3_logic_delete.sql`
* 作用：补齐上述5张表 `is_deleted` 字段、清洗 NULL、统一默认值为 `0`。
