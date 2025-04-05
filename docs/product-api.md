# 产品管理API接口文档

本文档详细描述了产品管理系统的API接口，包括产品的增删查改和按用户ID查询产品列表等功能。

## 目录

- [通用说明](#通用说明)
- [数据结构](#数据结构)
- [接口列表](#接口列表)
  - [创建产品](#创建产品)
  - [更新产品](#更新产品)
  - [查询产品详情](#查询产品详情)
  - [删除产品](#删除产品)
  - [查询用户的产品列表](#查询用户的产品列表)
  - [按分类查询产品](#按分类查询产品)
  - [查询所有产品](#查询所有产品)
  - [分页查询产品](#分页查询产品)
- [错误码说明](#错误码说明)

## 通用说明

### 基础URL

所有API的基础URL为：`/api/products`

### 请求方式

API使用RESTful风格，主要使用以下HTTP方法：

- `GET`：查询资源
- `POST`：创建资源
- `PUT`：更新资源
- `DELETE`：删除资源

### 响应格式

所有API响应均使用JSON格式，基本结构如下：

```json
{
  "code": 0,         // 状态码，0表示成功，非0表示失败
  "message": "success", // 状态消息
  "data": {          // 响应数据，根据接口不同而不同
    // 具体数据
  }
}
```

## 数据结构

### 产品（Product）

| 字段名 | 类型 | 描述 |
| ----- | ---- | ---- |
| id | Long | 产品ID |
| userId | Long | 用户ID |
| name | String | 产品名称 |
| description | String | 产品描述 |
| price | BigDecimal | 产品价格 |
| stock | Integer | 产品库存 |
| category | String | 产品分类 |
| status | Integer | 产品状态：0-下架，1-上架 |
| isDeleted | Integer | 删除标识：0-未删除，1-已删除 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

## 接口列表

### 创建产品

#### 接口说明

创建一个新的产品。

#### 请求方式

- **URL**: `/api/products`
- **Method**: POST
- **Content-Type**: application/json

#### 请求参数

```json
{
  "userId": 123,
  "name": "示例产品",
  "description": "这是一个示例产品",
  "price": 99.99,
  "stock": 100,
  "category": "电子产品",
  "status": 1
}
```

| 参数名 | 类型 | 必填 | 描述 |
| ----- | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID |
| name | String | 是 | 产品名称 |
| description | String | 否 | 产品描述 |
| price | BigDecimal | 是 | 产品价格 |
| stock | Integer | 是 | 产品库存 |
| category | String | 否 | 产品分类 |
| status | Integer | 否 | 产品状态：0-下架，1-上架，默认为1 |

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 123,
    "name": "示例产品",
    "description": "这是一个示例产品",
    "price": 99.99,
    "stock": 100,
    "category": "电子产品",
    "status": 1,
    "isDeleted": 0,
    "createTime": "2023-04-01T12:00:00",
    "updateTime": "2023-04-01T12:00:00"
  }
}
```

### 更新产品

#### 接口说明

更新指定ID的产品信息。

#### 请求方式

- **URL**: `/api/products/{id}`
- **Method**: PUT
- **Content-Type**: application/json

#### 请求参数

```json
{
  "name": "更新后的产品名称",
  "description": "更新后的产品描述",
  "price": 199.99,
  "stock": 200,
  "category": "电子产品",
  "status": 1
}
```

| 参数名 | 类型 | 必填 | 描述 |
| ----- | ---- | ---- | ---- |
| id | Long | 是 | 产品ID（URL路径参数） |
| name | String | 否 | 产品名称 |
| description | String | 否 | 产品描述 |
| price | BigDecimal | 否 | 产品价格 |
| stock | Integer | 否 | 产品库存 |
| category | String | 否 | 产品分类 |
| status | Integer | 否 | 产品状态：0-下架，1-上架 |

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": true
}
```

### 查询产品详情

#### 接口说明

查询指定ID的产品详情。

#### 请求方式

- **URL**: `/api/products/{id}`
- **Method**: GET

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
| ----- | ---- | ---- | ---- |
| id | Long | 是 | 产品ID（URL路径参数） |

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 123,
    "name": "示例产品",
    "description": "这是一个示例产品",
    "price": 99.99,
    "stock": 100,
    "category": "电子产品",
    "status": 1,
    "isDeleted": 0,
    "createTime": "2023-04-01T12:00:00",
    "updateTime": "2023-04-01T12:00:00"
  }
}
```

### 删除产品

#### 接口说明

删除指定ID的产品（逻辑删除）。

#### 请求方式

- **URL**: `/api/products/{id}`
- **Method**: DELETE

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
| ----- | ---- | ---- | ---- |
| id | Long | 是 | 产品ID（URL路径参数） |

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": true
}
```

### 查询用户的产品列表

#### 接口说明

查询指定用户ID的产品列表。

#### 请求方式

- **URL**: `/api/products/user/{userId}`
- **Method**: GET

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
| ----- | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID（URL路径参数） |

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 123,
      "name": "示例产品1",
      "description": "这是示例产品1",
      "price": 99.99,
      "stock": 100,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-01T12:00:00",
      "updateTime": "2023-04-01T12:00:00"
    },
    {
      "id": 2,
      "userId": 123,
      "name": "示例产品2",
      "description": "这是示例产品2",
      "price": 199.99,
      "stock": 200,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-02T12:00:00",
      "updateTime": "2023-04-02T12:00:00"
    }
  ]
}
```

### 按分类查询产品

#### 接口说明

查询指定分类的产品列表。

#### 请求方式

- **URL**: `/api/products/category/{category}`
- **Method**: GET

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
| ----- | ---- | ---- | ---- |
| category | String | 是 | 产品分类（URL路径参数） |

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 123,
      "name": "示例产品1",
      "description": "这是示例产品1",
      "price": 99.99,
      "stock": 100,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-01T12:00:00",
      "updateTime": "2023-04-01T12:00:00"
    },
    {
      "id": 2,
      "userId": 456,
      "name": "示例产品2",
      "description": "这是示例产品2",
      "price": 199.99,
      "stock": 200,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-02T12:00:00",
      "updateTime": "2023-04-02T12:00:00"
    }
  ]
}
```

### 查询所有产品

#### 接口说明

查询所有未删除的产品列表。

#### 请求方式

- **URL**: `/api/products`
- **Method**: GET

#### 请求参数

无

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 123,
      "name": "示例产品1",
      "description": "这是示例产品1",
      "price": 99.99,
      "stock": 100,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-01T12:00:00",
      "updateTime": "2023-04-01T12:00:00"
    },
    {
      "id": 2,
      "userId": 456,
      "name": "示例产品2",
      "description": "这是示例产品2",
      "price": 199.99,
      "stock": 200,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-02T12:00:00",
      "updateTime": "2023-04-02T12:00:00"
    }
  ]
}
```

### 分页查询产品

#### 接口说明

分页查询产品列表。

#### 请求方式

- **URL**: `/api/products/page`
- **Method**: GET

#### 请求参数

| 参数名 | 类型 | 必填 | 描述 |
| ----- | ---- | ---- | ---- |
| pageNum | Integer | 否 | 页码，默认为1 |
| pageSize | Integer | 否 | 每页数量，默认为10 |

#### 响应结果

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 123,
      "name": "示例产品1",
      "description": "这是示例产品1",
      "price": 99.99,
      "stock": 100,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-01T12:00:00",
      "updateTime": "2023-04-01T12:00:00"
    },
    {
      "id": 2,
      "userId": 456,
      "name": "示例产品2",
      "description": "这是示例产品2",
      "price": 199.99,
      "stock": 200,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2023-04-02T12:00:00",
      "updateTime": "2023-04-02T12:00:00"
    }
  ]
}
```

## 错误码说明

| 错误码 | 说明 | 处理建议 |
| ----- | ---- | ------- |
| 0 | 成功 | 请求成功处理 |
| 400 | 请求参数错误 | 检查请求参数是否正确 |
| 404 | 资源不存在 | 检查请求的资源ID是否存在 |
| 500 | 服务器内部错误 | 联系后端开发人员排查问题 | 