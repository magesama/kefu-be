# kefu 数据库结构文档

本文档描述了kefu系统的数据库结构，基于实体类定义创建的表结构。

## 数据库概述

kefu数据库使用MySQL，字符集为utf8mb4，排序规则为utf8mb4_unicode_ci，支持完整的Unicode字符（包括emoji）。

## 表结构

系统包含三个主要表：用户表、产品表和用户文档表。

### 1. 用户表 (user)

存储系统用户信息，包括普通用户和管理员。

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|-------|------|------|-------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | | 用户名 |
| password | VARCHAR(100) | NOT NULL | | 密码 |
| phone | VARCHAR(20) | | NULL | 手机号 |
| email | VARCHAR(100) | | NULL | 邮箱 |
| balance | DECIMAL(10, 2) | NOT NULL | 0.00 | 账户余额 |
| status | TINYINT(4) | NOT NULL | 1 | 用户状态：0-禁用，1-正常 |
| role | TINYINT(4) | NOT NULL | 0 | 用户角色：0-普通用户，1-管理员 |
| create_time | DATETIME | NOT NULL | | 创建时间 |
| update_time | DATETIME | NOT NULL | | 更新时间 |

索引：
- 主键索引：`id`
- 唯一索引：`username`

### 2. 产品表 (product)

存储系统中的产品信息。

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|-------|------|------|-------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | | 产品ID |
| user_id | BIGINT(20) | NOT NULL | | 所属用户ID |
| name | VARCHAR(100) | NOT NULL | | 产品名称 |
| description | TEXT | | NULL | 产品描述 |
| price | DECIMAL(10, 2) | NOT NULL | | 产品价格 |
| stock | INT(11) | NOT NULL | 0 | 库存数量 |
| category | VARCHAR(50) | | NULL | 产品分类 |
| status | TINYINT(4) | NOT NULL | 1 | 产品状态：0-下架，1-上架 |
| is_deleted | TINYINT(4) | NOT NULL | 0 | 是否删除：0-未删除，1-已删除 |
| create_time | DATETIME | NOT NULL | | 创建时间 |
| update_time | DATETIME | NOT NULL | | 更新时间 |

索引：
- 主键索引：`id`
- 普通索引：`user_id`, `category`, `status`

### 3. 用户文档表 (user_document)

存储用户上传的文档信息。

| 字段名 | 类型 | 约束 | 默认值 | 说明 |
|-------|------|------|-------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | | 文档ID |
| user_id | BIGINT(20) | NOT NULL | | 用户ID |
| product_id | BIGINT(20) | | NULL | 产品ID |
| product_name | VARCHAR(100) | | NULL | 产品名称 |
| shop_id | BIGINT(20) | | NULL | 店铺ID |
| shop_name | VARCHAR(100) | | NULL | 店铺名称 |
| document_name | VARCHAR(255) | NOT NULL | | 文档名称 |
| document_type | VARCHAR(50) | | NULL | 文档类型 |
| file_size | BIGINT(20) | NOT NULL | | 文件大小(字节) |
| file_url | VARCHAR(500) | NOT NULL | | MinIO文件URL |
| description | TEXT | | NULL | 文档描述 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 是否删除：0-未删除，1-已删除 |
| create_time | DATETIME | NOT NULL | | 创建时间 |
| update_time | DATETIME | NOT NULL | | 更新时间 |

索引：
- 主键索引：`id`
- 普通索引：`user_id`, `product_id`

## 表关系

系统中的表之间存在逻辑关系，但不使用外键约束进行强制：

1. **用户与产品**：一对多关系
   - 一个用户可以拥有多个产品
   - 每个产品只属于一个用户
   - 通过 `product.user_id` 字段关联 `user.id`

2. **用户与文档**：一对多关系
   - 一个用户可以上传多个文档
   - 每个文档只属于一个用户
   - 通过 `user_document.user_id` 字段关联 `user.id`

3. **产品与文档**：一对多关系
   - 一个产品可以关联多个文档
   - 每个文档可以关联一个产品
   - 通过 `user_document.product_id` 字段关联 `product.id`

## 初始数据

系统初始化时会创建一个管理员用户：

```sql
INSERT INTO `user` (`username`, `password`, `phone`, `email`, `balance`, `status`, `role`, `create_time`, `update_time`) 
VALUES ('admin', '123456', '13800138000', 'admin@example.com', 0.00, 1, 1, NOW(), NOW());
```

## 注意事项

1. **密码安全**：实际应用中，密码应该使用加密存储，而不是明文。
2. **不使用外键约束**：为了提高性能和灵活性，系统不使用外键约束，而是在应用层面维护数据一致性。
3. **逻辑删除**：`is_deleted`字段用于逻辑删除，避免物理删除数据。
4. **时间字段**：所有表都包含`create_time`和`update_time`字段，用于记录数据的创建和更新时间。
5. **索引设计**：根据查询需求，添加了适当的索引以提高查询性能。 