# 客服系统

## 项目简介
本项目是一个基于Spring Boot的智能客服系统，集成了向量数据库和大语言模型，支持智能问答、用户管理、产品管理等功能。

## 技术栈
- 后端：Spring Boot 2.7.x
- 数据库：MySQL 8.0
- ORM框架：MyBatis-Plus
- 向量数据库：Elasticsearch
- 大语言模型：通义千问

## 功能模块

### 1. 用户管理
- 用户注册与登录
- 用户信息查询与修改
- 用户角色管理（普通用户/管理员）
- 账户充值

### 2. 产品管理
- 产品信息的增删改查
- 产品分类管理
- 产品库存管理

### 3. 智能客服
- 基于向量数据库的相似问题检索
- 结合大语言模型的智能回答
- 多轮对话上下文管理
- 知识库自动更新

## API接口文档

### 用户管理接口

#### 1. 用户注册
- URL: `/api/user/register`
- 方法: POST
- 请求参数:
  ```json
  {
    "username": "用户名",
    "password": "密码",
    "confirmPassword": "确认密码",
    "phone": "手机号",
    "email": "邮箱",
    "role": 0  // 用户角色：0-普通用户，1-管理员，默认为0
  }
  ```
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "userId": 1
    }
  }
  ```

#### 2. 用户登录
- URL: `/api/user/login`
- 方法: POST
- 请求参数:
  ```json
  {
    "username": "用户名",
    "password": "密码"
  }
  ```
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "username": "用户名",
      "phone": "手机号",
      "email": "邮箱",
      "balance": 0.00,
      "status": 1,
      "role": 0,  // 用户角色：0-普通用户，1-管理员
      "createTime": "2023-01-01 12:00:00"
    }
  }
  ```

#### 3. 获取用户信息
- URL: `/api/user/info/{userId}`
- 方法: GET
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "username": "用户名",
      "phone": "手机号",
      "email": "邮箱",
      "balance": 0.00,
      "status": 1,
      "role": 0,  // 用户角色：0-普通用户，1-管理员
      "createTime": "2023-01-01 12:00:00"
    }
  }
  ```

#### 4. 用户充值
- URL: `/api/user/recharge/{userId}`
- 方法: POST
- 请求参数:
  ```json
  {
    "amount": 100.00,
    "payType": 1,
    "remark": "充值备注"
  }
  ```
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "balance": 100.00
    }
  }
  ```

#### 5. 获取用户列表
- URL: `/api/user/list`
- 方法: GET
- 请求参数:
  - username: 用户名（可选，模糊查询）
  - status: 状态（可选，0-禁用，1-正常）
  - role: 角色（可选，0-普通用户，1-管理员）
  - page: 页码，默认1
  - size: 每页大小，默认10
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "id": 1,
        "username": "用户名1",
        "phone": "手机号1",
        "email": "邮箱1",
        "balance": 100.00,
        "status": 1,
        "role": 0,
        "createTime": "2023-01-01 12:00:00"
      },
      {
        "id": 2,
        "username": "用户名2",
        "phone": "手机号2",
        "email": "邮箱2",
        "balance": 200.00,
        "status": 1,
        "role": 1,
        "createTime": "2023-01-02 12:00:00"
      }
    ]
  }
  ```

#### 6. 获取用户总数
- URL: `/api/user/count`
- 方法: GET
- 请求参数:
  - username: 用户名（可选，模糊查询）
  - status: 状态（可选，0-禁用，1-正常）
  - role: 角色（可选，0-普通用户，1-管理员）
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": 100
  }
  ```

#### 7. 更新用户角色
- URL: `/api/user/{userId}/role`
- 方法: PUT
- 请求参数:
  - role: 角色值（0-普通用户，1-管理员）
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": true
  }
  ```

### 产品管理接口

// 产品管理接口文档...

### 智能客服接口

#### 1. 智能问答
- URL: `/api/chat/hybrid-answer`
- 方法: POST
- 请求参数:
  ```json
  {
    "question": "问题内容",
    "userId": 1,
    "tableId": "chat123",  // 聊天窗口ID，用于标识多轮对话的上下文
    "productName": "产品名称",
    "shopName": "店铺名称"
  }
  ```
- 响应结果:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "answer": "回答内容",
      "documents": [
        {
          "id": 1,
          "title": "文档标题",
          "content": "文档内容片段",
          "score": 0.95
        }
      ]
    }
  }
  ```

## 数据库设计

### 用户表(user)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键ID |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| balance | DECIMAL(10,2) | 账户余额 |
| status | TINYINT | 用户状态：0-禁用，1-正常 |
| role | TINYINT | 用户角色：0-普通用户，1-管理员 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 产品表(product)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键ID |
| user_id | BIGINT | 所属用户ID |
| name | VARCHAR(100) | 产品名称 |
| description | TEXT | 产品描述 |
| price | DECIMAL(10,2) | 产品价格 |
| stock | INT | 库存数量 |
| category | VARCHAR(50) | 产品分类 |
| status | TINYINT | 产品状态：0-下架，1-上架 |
| is_deleted | TINYINT | 是否删除：0-未删除，1-已删除 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 充值记录表(recharge_record)
| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 主键ID |
| user_id | BIGINT | 用户ID |
| amount | DECIMAL(10,2) | 充值金额 |
| before_balance | DECIMAL(10,2) | 充值前余额 |
| after_balance | DECIMAL(10,2) | 充值后余额 |
| status | TINYINT | 充值状态：0-失败，1-成功 |
| pay_type | TINYINT | 支付方式：1-支付宝，2-微信，3-银行卡 |
| trade_no | VARCHAR(50) | 交易流水号 |
| remark | VARCHAR(200) | 备注 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

## 部署说明

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Elasticsearch 7.x

### 部署步骤
1. 克隆代码到本地
2. 修改配置文件 `application.yml`，配置数据库连接信息
3. 执行SQL脚本，初始化数据库
4. 使用Maven打包：`mvn clean package`
5. 运行应用：`java -jar kefu-0.0.1-SNAPSHOT.jar`

## 更新日志

### v1.0.0 (2023-01-01)
- 初始版本发布
- 实现基本的用户管理、产品管理功能
- 集成向量数据库和大语言模型

### v1.1.0 (2023-03-14)
- 新增用户角色管理功能
- 优化智能问答的多轮对话体验
- 修复已知BUG
