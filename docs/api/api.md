# 智能客服系统 API 接口文档

## 目录

- [用户管理](#用户管理)
- [知识库管理](#知识库管理)
- [智能问答](#智能问答)
- [统计分析](#统计分析)

## 接口规范

### 请求格式

所有API请求均使用JSON格式，Content-Type为application/json

### 响应格式

```json
{
  "code": 0,           // 状态码，0表示成功，非0表示失败
  "message": "success", // 状态描述
  "data": {}           // 返回数据，可能是对象或数组
}
```

### 错误码说明

| 错误码 | 说明 |
| ----- | ---- |
| 0     | 成功 |
| 1001  | 参数错误 |
| 1002  | 用户未登录 |
| 2001  | 知识库操作失败 |
| 3001  | 系统内部错误 |

## 用户管理

### 用户注册

- **URL**: `/api/user/register`
- **Method**: `POST`
- **请求参数**:

```json
{
  "username": "test_user",
  "password": "password123",
  "email": "test@example.com"
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 123,
    "username": "test_user"
  }
}
```

### 用户登录

- **URL**: `/api/user/login`
- **Method**: `POST`
- **请求参数**:

```json
{
  "username": "test_user",
  "password": "password123"
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 123,
    "username": "test_user",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 获取用户信息

- **URL**: `/api/user/info`
- **Method**: `GET`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 123,
    "username": "test_user",
    "email": "test@example.com",
    "balance": 100.00,
    "totalRecharge": 200.00
  }
}
```

## 知识库管理

### 创建产品

- **URL**: `/api/product/create`
- **Method**: `POST`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **请求参数**:

```json
{
  "productName": "智能手机",
  "description": "智能手机产品知识库"
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "productId": 456,
    "productName": "智能手机"
  }
}
```

### 获取产品列表

- **URL**: `/api/product/list`
- **Method**: `GET`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "productId": 456,
      "productName": "智能手机",
      "description": "智能手机产品知识库",
      "createTime": "2023-03-12 10:00:00"
    }
  ]
}
```

### 上传知识库文档

- **URL**: `/api/knowledge/upload`
- **Method**: `POST`
- **请求头**: 
  - `Authorization: Bearer {token}`
  - `Content-Type: multipart/form-data`
- **请求参数**:
  - `file`: 文件对象
  - `productId`: 产品ID
  - `fileName`: 文件名称

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "knowledgeId": 789,
    "fileName": "手机使用说明书.pdf",
    "filePath": "user_123/product_456/手机使用说明书.pdf"
  }
}
```

### 获取知识库文档列表

- **URL**: `/api/knowledge/list`
- **Method**: `GET`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **请求参数**:
  - `productId`: 产品ID (可选)
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "knowledgeId": 789,
      "productId": 456,
      "productName": "智能手机",
      "fileName": "手机使用说明书.pdf",
      "filePath": "user_123/product_456/手机使用说明书.pdf",
      "createTime": "2023-03-12 11:00:00"
    }
  ]
}
```

### 删除知识库文档

- **URL**: `/api/knowledge/delete/{knowledgeId}`
- **Method**: `DELETE`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

### 获取知识片段列表

- **URL**: `/api/knowledge/fragments`
- **Method**: `GET`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **请求参数**:
  - `knowledgeId`: 知识库ID
  - `page`: 页码，默认1
  - `size`: 每页数量，默认10
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 100,
    "list": [
      {
        "fragmentId": 1001,
        "knowledgeId": 789,
        "question": "如何重启手机？",
        "answer": "长按电源键3秒，然后选择重启选项。",
        "createTime": "2023-03-12 12:00:00"
      }
    ]
  }
}
```

### 修改知识片段

- **URL**: `/api/knowledge/fragment/update`
- **Method**: `PUT`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **请求参数**:

```json
{
  "fragmentId": 1001,
  "question": "如何重启智能手机？",
  "answer": "长按电源键3-5秒，然后在屏幕上选择重启选项即可。"
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

## 智能问答

### 智能问答接口

- **URL**: `/api/chat`
- **Method**: `POST`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **请求参数**:

```json
{
  "question": "如何重启手机？",
  "productId": 456  // 可选，如果不提供则进行意图识别
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "您可以通过长按电源键3秒，然后选择重启选项来重启您的手机。",
    "references": [
      {
        "fragmentId": 1001,
        "question": "如何重启手机？",
        "similarity": 0.95
      }
    ]
  }
}
```

## 统计分析

### 获取用户请求统计

- **URL**: `/api/stats/requests`
- **Method**: `GET`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **请求参数**:
  - `startDate`: 开始日期，格式：yyyy-MM-dd
  - `endDate`: 结束日期，格式：yyyy-MM-dd
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "date": "2023-03-12",
      "requestCount": 56
    },
    {
      "date": "2023-03-13",
      "requestCount": 78
    }
  ]
}
```

### 获取账户余额

- **URL**: `/api/user/balance`
- **Method**: `GET`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "balance": 100.00,
    "totalRecharge": 200.00
  }
}
```

### 充值接口

- **URL**: `/api/user/recharge`
- **Method**: `POST`
- **请求头**: 
  - `Authorization: Bearer {token}`
- **请求参数**:

```json
{
  "amount": 50.00
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "orderId": "R20230312123456",
    "amount": 50.00,
    "payUrl": "https://example.com/pay?orderId=R20230312123456"
  }
}
``` 