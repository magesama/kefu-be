# 产品API接口文档

本文档详细描述了系统中的产品相关API接口，包括产品的创建、查询、更新和删除等功能。

## 目录

- [接口概述](#接口概述)
- [基础接口](#基础接口)
  - [创建产品](#创建产品)
  - [更新产品](#更新产品)
  - [根据ID查询产品](#根据id查询产品)
  - [根据ID删除产品](#根据id删除产品)
  - [根据用户ID查询产品列表](#根据用户id查询产品列表)
  - [根据分类查询产品列表](#根据分类查询产品列表)
  - [查询所有产品](#查询所有产品)
  - [分页查询产品](#分页查询产品)
- [扩展接口](#扩展接口)
  - [获取产品选项列表](#获取产品选项列表)
  - [获取产品问答对](#获取产品问答对)
- [错误码说明](#错误码说明)

## 接口概述

产品API提供了对产品资源的完整管理功能，包括：

1. **基础CRUD操作**：创建、读取、更新和删除产品
2. **列表查询**：按用户、分类或分页查询产品列表
3. **扩展功能**：获取产品选项列表（用于下拉框）、获取产品相关的问答对

所有接口都遵循RESTful设计原则，使用标准的HTTP方法和状态码。

## 基础接口

### 创建产品

#### 接口说明

创建一个新的产品。

#### 请求方式

```
POST /api/products
```

#### 请求参数

请求体为JSON格式，包含以下字段：

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Long | 是 | 所属用户ID |
| name | String | 是 | 产品名称 |
| description | String | 否 | 产品描述 |
| price | BigDecimal | 是 | 产品价格 |
| stock | Integer | 否 | 库存数量，默认为0 |
| category | String | 否 | 产品分类 |
| status | Integer | 否 | 产品状态：0-下架，1-上架，默认为1 |

#### 请求示例

```json
{
  "userId": 1,
  "name": "智能手机X1",
  "description": "最新款智能手机，搭载高性能处理器",
  "price": 3999.00,
  "stock": 100,
  "category": "电子产品",
  "status": 1
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Object | 响应数据，创建成功的产品信息 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "智能手机X1",
    "description": "最新款智能手机，搭载高性能处理器",
    "price": 3999.00,
    "stock": 100,
    "category": "电子产品",
    "status": 1,
    "isDeleted": 0,
    "createTime": "2025-03-16T10:30:00",
    "updateTime": "2025-03-16T10:30:00"
  }
}
```

### 更新产品

#### 接口说明

更新指定ID的产品信息。

#### 请求方式

```
PUT /api/products/{id}
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| id | Long | 是 | 产品ID（路径参数） |

请求体为JSON格式，包含要更新的字段：

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| name | String | 否 | 产品名称 |
| description | String | 否 | 产品描述 |
| price | BigDecimal | 否 | 产品价格 |
| stock | Integer | 否 | 库存数量 |
| category | String | 否 | 产品分类 |
| status | Integer | 否 | 产品状态：0-下架，1-上架 |

#### 请求示例

```json
{
  "name": "智能手机X1 Pro",
  "price": 4299.00,
  "stock": 50
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Boolean | 响应数据，true表示更新成功 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 根据ID查询产品

#### 接口说明

根据ID查询产品详细信息。

#### 请求方式

```
GET /api/products/{id}
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| id | Long | 是 | 产品ID（路径参数） |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Object | 响应数据，产品详细信息 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "智能手机X1 Pro",
    "description": "最新款智能手机，搭载高性能处理器",
    "price": 4299.00,
    "stock": 50,
    "category": "电子产品",
    "status": 1,
    "isDeleted": 0,
    "createTime": "2025-03-16T10:30:00",
    "updateTime": "2025-03-16T11:15:00"
  }
}
```

### 根据ID删除产品

#### 接口说明

根据ID删除产品（逻辑删除）。

#### 请求方式

```
DELETE /api/products/{id}
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| id | Long | 是 | 产品ID（路径参数） |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Boolean | 响应数据，true表示删除成功 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

### 根据用户ID查询产品列表

#### 接口说明

查询指定用户ID的所有产品。

#### 请求方式

```
GET /api/products/user/{userId}
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| userId | Long | 是 | 用户ID（路径参数） |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Array | 响应数据，产品列表 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "name": "智能手机X1 Pro",
      "description": "最新款智能手机，搭载高性能处理器",
      "price": 4299.00,
      "stock": 50,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T10:30:00",
      "updateTime": "2025-03-16T11:15:00"
    },
    {
      "id": 2,
      "userId": 1,
      "name": "智能手表W1",
      "description": "高端智能手表，支持心率监测",
      "price": 1299.00,
      "stock": 80,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T14:20:00",
      "updateTime": "2025-03-16T14:20:00"
    }
  ]
}
```

### 根据分类查询产品列表

#### 接口说明

查询指定分类的所有产品。

#### 请求方式

```
GET /api/products/category/{category}
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| category | String | 是 | 产品分类（路径参数） |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Array | 响应数据，产品列表 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "name": "智能手机X1 Pro",
      "description": "最新款智能手机，搭载高性能处理器",
      "price": 4299.00,
      "stock": 50,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T10:30:00",
      "updateTime": "2025-03-16T11:15:00"
    },
    {
      "id": 2,
      "userId": 1,
      "name": "智能手表W1",
      "description": "高端智能手表，支持心率监测",
      "price": 1299.00,
      "stock": 80,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T14:20:00",
      "updateTime": "2025-03-16T14:20:00"
    }
  ]
}
```

### 查询所有产品

#### 接口说明

查询所有产品。

#### 请求方式

```
GET /api/products
```

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Array | 响应数据，产品列表 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "name": "智能手机X1 Pro",
      "description": "最新款智能手机，搭载高性能处理器",
      "price": 4299.00,
      "stock": 50,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T10:30:00",
      "updateTime": "2025-03-16T11:15:00"
    },
    {
      "id": 2,
      "userId": 1,
      "name": "智能手表W1",
      "description": "高端智能手表，支持心率监测",
      "price": 1299.00,
      "stock": 80,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T14:20:00",
      "updateTime": "2025-03-16T14:20:00"
    }
  ]
}
```

### 分页查询产品

#### 接口说明

分页查询产品列表。

#### 请求方式

```
GET /api/products/page
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| pageNum | Integer | 否 | 页码，默认为1 |
| pageSize | Integer | 否 | 每页数量，默认为10 |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Array | 响应数据，产品列表 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userId": 1,
      "name": "智能手机X1 Pro",
      "description": "最新款智能手机，搭载高性能处理器",
      "price": 4299.00,
      "stock": 50,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T10:30:00",
      "updateTime": "2025-03-16T11:15:00"
    },
    {
      "id": 2,
      "userId": 1,
      "name": "智能手表W1",
      "description": "高端智能手表，支持心率监测",
      "price": 1299.00,
      "stock": 80,
      "category": "电子产品",
      "status": 1,
      "isDeleted": 0,
      "createTime": "2025-03-16T14:20:00",
      "updateTime": "2025-03-16T14:20:00"
    }
  ]
}
```

## 扩展接口

### 获取产品选项列表

#### 接口说明

获取产品列表，仅包含ID和名称，用于下拉选择框等场景。

#### 请求方式

```
GET /api/products/options
```

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Array | 响应数据，产品选项列表 |
| data[].label | String | 选项标签（产品名称） |
| data[].value | String | 选项值（产品ID） |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "label": "智能手机X1 Pro",
      "value": "1"
    },
    {
      "label": "智能手表W1",
      "value": "2"
    }
  ]
}
```

### 获取产品问答对

#### 接口说明

获取指定产品ID的所有问答对，仅包含问题和答案字段。

#### 请求方式

```
GET /api/products/{productId}/qa
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| ----- | ---- | ---- | ---- |
| productId | Long | 是 | 产品ID（路径参数） |

#### 响应参数

| 参数名 | 类型 | 说明 |
| ----- | ---- | ---- |
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 状态信息，成功时为"success"，失败时为错误信息 |
| data | Array | 响应数据，问答对列表 |
| data[].question | String | 问题 |
| data[].answer | String | 答案 |

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "question": "这款手机的电池容量是多少？",
      "answer": "智能手机X1 Pro配备了4500mAh大容量电池，支持快充技术，充电30分钟可达80%电量。"
    },
    {
      "question": "这款手机支持5G网络吗？",
      "answer": "是的，智能手机X1 Pro全面支持5G网络，兼容全球主流5G频段，提供极速网络体验。"
    }
  ]
}
```

## 错误码说明

| 错误码 | 说明 |
| ----- | ---- |
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 | 