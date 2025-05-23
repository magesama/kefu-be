# 智能客服系统设计方案

## 1. 系统架构

### 1.1 整体架构

智能客服系统采用微服务架构设计，主要包含以下几个核心模块：

- **用户管理模块**：负责用户注册、登录、权限控制等功能
- **知识库管理模块**：负责知识库文档的上传、拆分、向量化和管理
- **智能问答模块**：负责接收用户问题，进行意图识别、知识检索和回答生成
- **统计分析模块**：负责系统使用情况的统计和分析

### 1.2 技术栈

- **后端**：Spring Boot 2.7.18
- **数据库**：MySQL 8.0
- **搜索引擎**：ElasticSearch 7.10.0
- **对象存储**：MinIO RELEASE.2022-01-04T07-41-07Z
- **大模型服务**：阿里云DashScope大模型

### 1.3 系统架构图

```
+------------------+    +------------------+    +------------------+
|                  |    |                  |    |                  |
|   用户界面        |    |   管理界面        |    |   统计界面        |
|                  |    |                  |    |                  |
+--------+---------+    +--------+---------+    +--------+---------+
         |                       |                       |
         v                       v                       v
+------------------+    +------------------+    +------------------+
|                  |    |                  |    |                  |
|   用户管理模块     |    |   知识库管理模块   |    |   统计分析模块    |
|                  |    |                  |    |                  |
+--------+---------+    +--------+---------+    +--------+---------+
         |                       |                       |
         v                       v                       v
+------------------+    +------------------+    +------------------+
|                  |    |                  |    |                  |
|     MySQL        |    |  ElasticSearch   |    |     MinIO        |
|                  |    |                  |    |                  |
+------------------+    +------------------+    +------------------+
                                |
                                v
                        +------------------+
                        |                  |
                        |   智能问答模块     |
                        |                  |
                        +--------+---------+
                                 |
                                 v
                        +------------------+
                        |                  |
                        |   大模型服务      |
                        |                  |
                        +------------------+
```

## 2. 核心模块设计

### 2.1 用户管理模块

#### 2.1.1 功能描述

- 用户注册与登录
- 用户信息管理
- 用户权益管理（余额、充值等）

#### 2.1.2 数据流程

1. 用户注册：接收用户注册信息 -> 验证信息有效性 -> 密码加密 -> 保存用户信息 -> 返回注册结果
2. 用户登录：接收用户登录信息 -> 验证用户名密码 -> 生成JWT令牌 -> 返回登录结果
3. 用户信息查询：验证用户身份 -> 查询用户信息 -> 返回用户信息

### 2.2 知识库管理模块

#### 2.2.1 功能描述

- 产品管理
- 知识库文档上传与管理
- 文档拆分与向量化
- 知识片段管理

#### 2.2.2 数据流程

1. 文档上传：接收文档 -> 保存到MinIO -> 记录文档信息到MySQL
2. 文档拆分：读取文档 -> 调用大模型拆分文档 -> 提取问题和答案对
3. 向量化处理：调用大模型生成向量 -> 存储向量到ElasticSearch
4. 知识片段管理：增删改查知识片段 -> 更新ElasticSearch索引

### 2.3 智能问答模块

#### 2.3.1 功能描述

- 接收用户问题
- 意图识别
- 知识检索
- 回答生成

#### 2.3.2 数据流程

1. 问题接收：接收用户问题 -> 记录请求日志
2. 意图识别：分析问题 -> 判断是否包含产品信息
3. 知识检索：
   - 向量检索：将问题转换为向量 -> 在ES中进行向量相似度检索
   - 全文检索：在ES中进行全文检索
   - 结果融合：按照配置比例融合两种检索结果
4. 回答生成：将检索结果作为上下文 -> 调用大模型生成回答 -> 返回回答结果

### 2.4 统计分析模块

#### 2.4.1 功能描述

- 请求日志记录
- 用户请求统计
- 系统使用情况分析

#### 2.4.2 数据流程

1. 日志记录：拦截请求 -> 记录请求信息到ES
2. 请求统计：定时任务 -> 从ES聚合统计数据 -> 更新MySQL统计表
3. 统计查询：接收查询请求 -> 查询统计数据 -> 返回统计结果

## 3. 数据模型设计

### 3.1 MySQL数据模型

详见 `docs/database/mysql/init.sql`

### 3.2 ElasticSearch索引设计

详见 `docs/database/elasticsearch/indices.json`

## 4. 关键流程设计

### 4.1 知识库文档处理流程

```
上传文档 -> 保存到MinIO -> 
调用大模型拆分文档 -> 提取问题和答案对 -> 
调用大模型生成向量 -> 存储到ElasticSearch
```

### 4.2 智能问答流程

```
接收用户问题 -> 记录请求日志 -> 
意图识别 -> 确定产品ID -> 
向量检索 + 全文检索 -> 结果融合 -> 
调用大模型生成回答 -> 返回回答结果
```

### 4.3 请求统计流程

```
定时任务触发 -> 
从ES聚合当日各用户请求数 -> 
更新或插入MySQL统计表
```

## 5. 系统配置

### 5.1 ElasticSearch配置

```yaml
elasticsearch:
  host: localhost
  port: 9200
  username: elastic
  password: elastic
  connectTimeout: 5000
  socketTimeout: 60000
  connectionRequestTimeout: 5000
  maxConnTotal: 100
  maxConnPerRoute: 100
  indices:
    log: log_index
    knowledge: knowledge_base_index
```

### 5.2 MinIO配置

```yaml
minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: kefu
```

### 5.3 大模型配置

```yaml
dashscope:
  apiKey: your_api_key
  model:
    text: qwen-max
    embedding: text-embedding-v2
  vectorDimension: 768
```

## 6. 安全设计

### 6.1 用户认证与授权

- 采用JWT进行用户认证
- 基于RBAC的权限控制
- 接口访问频率限制

### 6.2 数据安全

- 用户密码加密存储
- 敏感信息脱敏处理
- 数据传输加密

## 7. 扩展性设计

### 7.1 水平扩展

- 服务无状态化设计
- 数据库读写分离
- ElasticSearch集群部署

### 7.2 功能扩展

- 插件化设计
- 配置中心动态调整参数
- 多种大模型支持 