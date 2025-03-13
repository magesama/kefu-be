# 用户管理API文档

本文档描述了用户管理相关的API接口，包括用户注册、登录、查询用户信息和充值等功能。

## 通用说明

### 基础URL

所有API的基础URL为：`http://localhost:8080`

### 响应格式

所有API的响应格式统一如下：

```json
{
  "code": 0,       // 响应码，0表示成功，非0表示失败
  "message": "success", // 响应消息
  "data": {}       // 响应数据，具体格式根据接口不同而不同
}
```

### 错误码说明

| 错误码 | 说明 |
| ----- | ---- |
| 0     | 成功 |
| 400   | 请求参数错误 |
| 401   | 未授权 |
| 403   | 禁止访问 |
| 404   | 资源不存在 |
| 500   | 服务器内部错误 |

## API列表

### 1. 用户注册

#### 请求

- 方法: `POST`
- URL: `/api/user/register`
- 内容类型: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| username | String | 是 | 用户名，长度4-20个字符 |
| password | String | 是 | 密码，长度6-20个字符 |
| confirmPassword | String | 是 | 确认密码，必须与密码一致 |
| phone | String | 否 | 手机号，格式为11位数字 |
| email | String | 否 | 邮箱地址 |

#### 请求示例

```json
{
  "username": "testuser",
  "password": "password123",
  "confirmPassword": "password123",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| userId | Long | 注册成功的用户ID |

#### 响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 1
  }
}
```

### 2. 用户登录

#### 请求

- 方法: `POST`
- URL: `/api/user/login`
- 内容类型: `application/json`

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

#### 请求示例

```json
{
  "username": "testuser",
  "password": "password123"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| id | Long | 用户ID |
| username | String | 用户名 |
| phone | String | 手机号 |
| email | String | 邮箱 |
| balance | BigDecimal | 账户余额 |
| status | Integer | 用户状态：0-禁用，1-正常 |
| createTime | String | 创建时间 |

#### 响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "balance": 0.00,
    "status": 1,
    "createTime": "2023-03-15T10:30:00"
  }
}
```

### 3. 获取用户信息

#### 请求

- 方法: `GET`
- URL: `/api/user/info/{userId}`

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| id | Long | 用户ID |
| username | String | 用户名 |
| phone | String | 手机号 |
| email | String | 邮箱 |
| balance | BigDecimal | 账户余额 |
| status | Integer | 用户状态：0-禁用，1-正常 |
| createTime | String | 创建时间 |

#### 响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "balance": 0.00,
    "status": 1,
    "createTime": "2023-03-15T10:30:00"
  }
}
```

### 4. 用户充值

#### 请求

- 方法: `POST`
- URL: `/api/user/recharge/{userId}`
- 内容类型: `application/json`

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| amount | BigDecimal | 是 | 充值金额，必须大于0 |
| payType | Integer | 是 | 充值方式：1-支付宝，2-微信，3-银行卡 |
| remark | String | 否 | 备注 |

#### 请求示例

```json
{
  "amount": 100.00,
  "payType": 1,
  "remark": "充值测试"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| balance | BigDecimal | 充值后的账户余额 |

#### 响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "balance": 100.00
  }
}
```

### 5. 检查用户名是否存在

#### 请求

- 方法: `GET`
- URL: `/api/user/check-username`

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| username | String | 是 | 要检查的用户名 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| exists | Boolean | 用户名是否已存在 |

#### 响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "exists": false
  }
}
```

### 6. 获取用户列表

#### 请求

- 方法: `GET`
- URL: `/api/user/list`

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| username | String | 否 | 用户名（模糊查询） |
| status | Integer | 否 | 用户状态：0-禁用，1-正常 |
| page | Integer | 否 | 页码，从1开始，默认为1 |
| size | Integer | 否 | 每页大小，默认为10 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| users | Array | 用户列表 |
| users[].id | Long | 用户ID |
| users[].username | String | 用户名 |
| users[].phone | String | 手机号 |
| users[].email | String | 邮箱 |
| users[].balance | BigDecimal | 账户余额 |
| users[].status | Integer | 用户状态：0-禁用，1-正常 |
| users[].createTime | String | 创建时间 |
| total | Long | 总记录数 |
| page | Integer | 当前页码 |
| size | Integer | 每页大小 |

#### 响应示例

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "users": [
      {
        "id": 1,
        "username": "testuser1",
        "phone": "13800138001",
        "email": "test1@example.com",
        "balance": 100.00,
        "status": 1,
        "createTime": "2023-03-15T10:30:00"
      },
      {
        "id": 2,
        "username": "testuser2",
        "phone": "13800138002",
        "email": "test2@example.com",
        "balance": 200.00,
        "status": 1,
        "createTime": "2023-03-15T11:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

## 错误响应示例

### 参数校验错误

```json
{
  "code": 400,
  "message": "用户名不能为空",
  "data": null
}
```

### 业务逻辑错误

```json
{
  "code": 500,
  "message": "用户名已存在",
  "data": null
}
```

### 系统错误

```json
{
  "code": 500,
  "message": "系统异常，请联系管理员",
  "data": null
}
``` 