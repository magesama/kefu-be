# Elasticsearch 管理系统接口文档

## 1. 概述

本文档描述了一个轻量级的 Elasticsearch 管理系统后端接口设计。该系统提供了基本的 Elasticsearch 管理功能，包括索引查询、数据展示、搜索筛选、向量检索等功能，是 Kibana 的简化替代方案。

## 2. 接口设计

### 2.1 基础路径

所有 API 接口都以 `/api/es` 为基础路径。

### 2.2 接口列表

#### 2.2.1 获取所有索引

- **URL**: `/api/es/indices`
- **Method**: `GET`
- **描述**: 获取 Elasticsearch 中所有索引的列表及其基本信息
- **请求参数**: 无
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "name": "log_index",
      "docsCount": 1250,
      "docsDeleted": 0,
      "storeSizeBytes": 1048576,
      "health": "green",
      "status": "open",
      "creationDate": "2023-03-15T10:00:00Z"
    },
    {
      "name": "knowledge_base_index",
      "docsCount": 500,
      "docsDeleted": 10,
      "storeSizeBytes": 524288,
      "health": "yellow",
      "status": "open",
      "creationDate": "2023-03-10T08:30:00Z"
    }
  ]
}
```

#### 2.2.2 创建自定义索引

- **URL**: `/api/es/create-index`
- **Method**: `POST`
- **描述**: 创建一个新的索引，支持自定义设置和映射
- **请求参数**:
  - `indexName`: 索引名称 (查询参数)
  - 请求体: 包含索引设置和映射的JSON

```json
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "smartcn_analyzer": {
          "type": "smartcn"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "smartcn_analyzer"
      },
      "content": {
        "type": "text",
        "analyzer": "smartcn_analyzer"
      },
      "userId": {
        "type": "keyword"
      },
      "createTime": {
        "type": "date"
      }
    }
  }
}
```

- **响应示例**:

```json
{
  "success": true,
  "message": "索引创建成功"
}
```

#### 2.2.3 删除索引

- **URL**: `/api/es/delete-index`
- **Method**: `DELETE`
- **描述**: 删除指定的索引
- **请求参数**:
  - `indexName`: 索引名称 (查询参数)
- **响应示例**:

```json
{
  "success": true,
  "message": "索引删除成功"
}
```

#### 2.2.4 获取索引详情

- **URL**: `/api/es/indices/{indexName}`
- **Method**: `GET`
- **描述**: 获取指定索引的详细信息，包括映射、设置等
- **请求参数**:
  - `indexName`: 索引名称 (路径参数)
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "name": "log_index",
    "mappings": {
      "properties": {
        "userId": {
          "type": "keyword"
        },
        "apiPath": {
          "type": "keyword"
        },
        "requestTime": {
          "type": "date"
        }
      }
    },
    "settings": {
      "number_of_shards": "3",
      "number_of_replicas": "1",
      "analysis": {
        "analyzer": {
          "smartcn_analyzer": {
            "type": "smartcn"
          }
        }
      }
    },
    "docsCount": 1250,
    "docsDeleted": 0,
    "storeSizeBytes": 1048576,
    "health": "green",
    "status": "open",
    "creationDate": "2023-03-15T10:00:00Z"
  }
}
```

#### 2.2.5 查询索引数据

- **URL**: `/api/es/indices/{indexName}/search`
- **Method**: `POST`
- **描述**: 根据条件查询索引中的数据
- **请求参数**:
  - `indexName`: 索引名称 (路径参数)
  - 请求体:

```json
{
  "query": {
    "term": {
      "userId": "123"
    }
  },
  "from": 0,
  "size": 10,
  "sort": [
    {
      "requestTime": {
        "order": "desc"
      }
    }
  ],
  "highlight": {
    "fields": {
      "content": {}
    }
  }
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 50,
    "hits": [
      {
        "id": "abc123",
        "source": {
          "userId": "123",
          "apiPath": "/api/user/info",
          "requestTime": "2023-03-15T12:30:45Z",
          "status": "success"
        },
        "highlight": {
          "content": ["这是<em>高亮</em>的内容"]
        }
      },
      {
        "id": "def456",
        "source": {
          "userId": "123",
          "apiPath": "/api/user/login",
          "requestTime": "2023-03-15T12:25:30Z",
          "status": "success"
        }
      }
    ]
  }
}
```

#### 2.2.6 高级搜索接口

- **URL**: `/api/es/indices/{indexName}/advanced-search`
- **Method**: `POST`
- **描述**: 提供更灵活的搜索功能，支持精确匹配和模糊匹配
- **请求参数**:
  - `indexName`: 索引名称 (路径参数)
  - 请求体:

```json
{
  "conditions": [
    {
      "field": "userId",
      "value": "123",
      "operator": "eq"  // eq: 等于, ne: 不等于, gt: 大于, lt: 小于, gte: 大于等于, lte: 小于等于, like: 模糊匹配
    },
    {
      "field": "apiPath",
      "value": "user",
      "operator": "like"
    },
    {
      "field": "status",
      "value": "success",
      "operator": "eq"
    }
  ],
  "logicalOperator": "and",  // and 或 or
  "from": 0,
  "size": 10,
  "sort": [
    {
      "field": "requestTime",
      "order": "desc"
    }
  ]
}
```

- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 25,
    "hits": [
      {
        "id": "abc123",
        "source": {
          "userId": "123",
          "apiPath": "/api/user/info",
          "requestTime": "2023-03-15T12:30:45Z",
          "status": "success"
        }
      },
      {
        "id": "def456",
        "source": {
          "userId": "123",
          "apiPath": "/api/user/login",
          "requestTime": "2023-03-15T12:25:30Z",
          "status": "success"
        }
      }
    ]
  }
}
```

#### 2.2.7 获取索引字段信息

- **URL**: `/api/es/indices/{indexName}/fields`
- **Method**: `GET`
- **描述**: 获取指定索引的所有字段信息
- **请求参数**:
  - `indexName`: 索引名称 (路径参数)
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "name": "userId",
      "type": "keyword"
    },
    {
      "name": "apiPath",
      "type": "keyword"
    },
    {
      "name": "requestTime",
      "type": "date"
    },
    {
      "name": "status",
      "type": "keyword"
    }
  ]
}
```

#### 2.2.8 获取集群健康状态

- **URL**: `/api/es/cluster/health`
- **Method**: `GET`
- **描述**: 获取 Elasticsearch 集群的健康状态
- **请求参数**: 无
- **响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "clusterName": "elasticsearch",
    "status": "green",
    "numberOfNodes": 1,
    "numberOfDataNodes": 1,
    "activePrimaryShards": 10,
    "activeShards": 10,
    "relocatingShards": 0,
    "initializingShards": 0,
    "unassignedShards": 0
  }
}
```

#### 2.2.9 生成向量并存储文档

- **URL**: `/api/es/vector-store-custom-index`
- **Method**: `POST`
- **描述**: 生成文本的向量表示并存储到指定索引中
- **请求参数**:
  - `indexName`: 索引名称 (查询参数)
  - 请求体:

```json
{
  "content": "如何申请退款？",
  "title": "退款申请流程",
  "category": "退款相关",
  "tags": ["退款", "售后"]
}
```

- **响应示例**:

```json
{
  "success": true,
  "id": "xxx",
  "indexName": "test_vectors",
  "vectorDimensions": 512
}
```

#### 2.2.10 向量相似度搜索

- **URL**: `/api/es/custom-vector-search`
- **Method**: `POST`
- **描述**: 在指定索引中进行向量相似度搜索
- **请求参数**:
  - `indexName`: 索引名称 (查询参数)
  - 请求体:

```json
{
  "query": "如何退款？商品不满意",
  "size": 5
}
```

- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "_id": "xxx",
      "_score": 0.95,
      "title": "退款申请流程",
      "content": "如何申请退款？",
      "category": "退款相关",
      "tags": ["退款", "售后"]
    }
  ],
  "query": "如何退款？商品不满意",
  "indexName": "test_vectors"
}
```

#### 2.2.11 全文检索

- **URL**: `/api/es/custom-text-search`
- **Method**: `POST`
- **描述**: 在指定索引中进行全文检索
- **请求参数**:
  - `indexName`: 索引名称 (查询参数)
  - 请求体:

```json
{
  "query": "退款",
  "fields": ["title", "content"],
  "size": 5
}
```

- **响应示例**:

```json
{
  "success": true,
  "data": [
    {
      "_id": "xxx",
      "_score": 0.8,
      "title": "退款申请流程",
      "content": "如何申请退款？",
      "category": "退款相关",
      "tags": ["退款", "售后"]
    }
  ],
  "query": "退款",
  "fields": ["title", "content"],
  "indexName": "test_vectors"
}
```

#### 2.2.12 执行原始DSL查询

- **URL**: `/api/es/dsl`
- **Method**: `POST`
- **描述**: 执行原始DSL查询，支持所有Elasticsearch操作，包括索引管理、文档增删改查等
- **请求参数**:
  - 请求体:

```json
{
  "endpoint": "log_index/_search",  // Elasticsearch API端点，例如 "_search", "my_index/_search", "_cat/indices" 等
  "method": "POST",                 // HTTP方法，例如 "GET", "POST", "PUT", "DELETE"
  "dslQuery": {                     // DSL查询JSON对象（可选，取决于请求类型）
    "query": {
      "match_all": {}
    },
    "size": 100
  }
}
```

- **响应示例**:

```json
{
  "success": true,
  "statusCode": 200,
  "result": {
    "took": 5,
    "timed_out": false,
    "_shards": {
      "total": 1,
      "successful": 1,
      "skipped": 0,
      "failed": 0
    },
    "hits": {
      "total": {
        "value": 1250,
        "relation": "eq"
      },
      "max_score": 1.0,
      "hits": [
        // 文档列表
      ]
    }
  }
}
```

- **常用DSL查询示例**:

1. 查询所有索引:
```json
{
  "endpoint": "_cat/indices?v",
  "method": "GET"
}
```

2. 创建索引:
```json
{
  "endpoint": "new_index",
  "method": "PUT",
  "dslQuery": {
    "settings": {
      "number_of_shards": 3,
      "number_of_replicas": 1
    },
    "mappings": {
      "properties": {
        "title": {
          "type": "text"
        },
        "content": {
          "type": "text"
        }
      }
    }
  }
}
```

3. 删除索引:
```json
{
  "endpoint": "index_to_delete",
  "method": "DELETE"
}
```

4. 添加文档:
```json
{
  "endpoint": "my_index/_doc",
  "method": "POST",
  "dslQuery": {
    "title": "测试文档",
    "content": "这是一个测试文档",
    "createTime": "2023-03-15T10:00:00Z"
  }
}
```

5. 更新文档:
```json
{
  "endpoint": "my_index/_doc/document_id",
  "method": "PUT",
  "dslQuery": {
    "title": "更新后的标题",
    "content": "更新后的内容",
    "updateTime": "2023-03-16T10:00:00Z"
  }
}
```

6. 删除文档:
```json
{
  "endpoint": "my_index/_doc/document_id",
  "method": "DELETE"
}
```

7. 复杂查询:
```json
{
  "endpoint": "my_index/_search",
  "method": "POST",
  "dslQuery": {
    "query": {
      "bool": {
        "must": [
          {
            "match": {
              "title": "测试"
            }
          }
        ],
        "filter": [
          {
            "range": {
              "createTime": {
                "gte": "2023-01-01",
                "lte": "2023-12-31"
              }
            }
          }
        ]
      }
    },
    "sort": [
      {
        "createTime": {
          "order": "desc"
        }
      }
    ],
    "from": 0,
    "size": 20
  }
}
```

## 3. 错误码说明

| 错误码 | 说明 |
| ----- | ---- |
| 0     | 成功 |
| 1001  | 参数错误 |
| 2001  | Elasticsearch 连接错误 |
| 2002  | 索引不存在 |
| 2003  | 查询语法错误 |
| 3001  | 系统内部错误 |

## 4. 前端集成指南

### 4.1 索引列表页面

前端可以通过调用 `/api/es/indices` 接口获取所有索引列表，并以表格形式展示。表格应包含以下列：
- 索引名称
- 文档数量
- 健康状态（可用不同颜色表示：绿色表示健康，黄色表示警告，红色表示错误）
- 存储大小
- 创建日期
- 操作（查看详情、查询数据）

### 4.2 索引详情页面

点击索引名称或详情按钮后，前端可以通过调用 `/api/es/indices/{indexName}` 接口获取索引详情，并以卡片形式展示索引的基本信息、映射和设置。

### 4.3 数据查询页面

数据查询页面应包含以下组件：
1. 索引选择器：用于选择要查询的索引
2. 查询条件构建器：用于构建查询条件
   - 支持添加多个条件
   - 每个条件包括字段名、操作符和值
   - 支持选择条件之间的逻辑关系（AND/OR）
3. 排序选项：用于设置排序字段和排序方向
4. 分页控件：用于控制分页
5. 结果表格：用于展示查询结果

前端可以通过调用 `/api/es/indices/{indexName}/fields` 接口获取索引的字段信息，用于构建查询条件。

然后，根据用户构建的查询条件，调用 `/api/es/indices/{indexName}/advanced-search` 接口获取查询结果。

### 4.4 向量搜索页面

向量搜索页面应包含以下组件：
1. 索引选择器：用于选择要搜索的索引
2. 查询输入框：用于输入查询文本
3. 结果数量选择器：用于设置返回结果数量
4. 结果列表：用于展示搜索结果

前端可以通过调用 `/api/es/custom-vector-search` 接口进行向量相似度搜索。

### 4.5 集群监控页面

前端可以通过调用 `/api/es/cluster/health` 接口获取集群健康状态，并以仪表盘形式展示。

## 5. 后端实现指南

### 5.1 模型定义

#### 5.1.1 请求模型

**SearchRequest.java**
```java
public class SearchRequest {
    private Map<String, Object> query;
    private int from = 0;
    private int size = 10;
    private List<Map<String, Map<String, String>>> sort;
    private Map<String, Object> highlight;
    
    // getters and setters
}
```

**AdvancedSearchRequest.java**
```java
public class AdvancedSearchRequest {
    private List<SearchCondition> conditions;
    private String logicalOperator = "and";
    private int from = 0;
    private int size = 10;
    private List<SortField> sort;
    
    // getters and setters
    
    @Data
    @Builder
    public static class SearchCondition {
        private String field;
        private Object value;
        private String operator;
    }
    
    @Data
    @Builder
    public static class SortField {
        private String field;
        private String order;
    }
}
```

#### 5.1.2 响应模型

**ApiResponse.java**
```java
@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage("success");
        response.setData(data);
        return response;
    }
    
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
```

**IndexInfo.java**
```java
@Data
@Builder
public class IndexInfo {
    private String name;
    private Long docsCount;
    private Long docsDeleted;
    private Long storeSizeBytes;
    private String health;
    private String status;
    private Date creationDate;
    private Map<String, Object> mappings;
    private Map<String, Object> settings;
}
```

**FieldInfo.java**
```java
@Data
@Builder
public class FieldInfo {
    private String name;
    private String type;
}
```

**SearchResult.java**
```java
@Data
@Builder
public class SearchResult {
    private long total;
    private List<Hit> hits;
    
    @Data
    @Builder
    public static class Hit {
        private String id;
        private Map<String, Object> source;
        private Map<String, List<String>> highlight;
    }
}
```

**ClusterHealth.java**
```java
@Data
@Builder
public class ClusterHealth {
    private String clusterName;
    private String status;
    private int numberOfNodes;
    private int numberOfDataNodes;
    private int activePrimaryShards;
    private int activeShards;
    private int relocatingShards;
    private int initializingShards;
    private int unassignedShards;
}
```

### 5.2 控制器实现

**ElasticsearchController.java**
```java
@RestController
@RequestMapping("/api/es")
public class ElasticsearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ElasticsearchAdminService elasticsearchAdminService;

    @Autowired
    private AliEmbeddingService aliEmbeddingService;

    @GetMapping("/indices")
    public ApiResponse<List<IndexInfo>> getAllIndices() {
        List<IndexInfo> indices = elasticsearchAdminService.getAllIndices();
        return ApiResponse.success(indices);
    }

    @GetMapping("/indices/{indexName}")
    public ApiResponse<IndexInfo> getIndexInfo(@PathVariable String indexName) {
        IndexInfo indexInfo = elasticsearchAdminService.getIndexInfo(indexName);
        return ApiResponse.success(indexInfo);
    }

    @PostMapping("/indices/{indexName}/search")
    public ApiResponse<SearchResult> searchIndex(
            @PathVariable String indexName,
            @RequestBody SearchRequest request) {
        SearchResult result = elasticsearchAdminService.searchIndex(indexName, request);
        return ApiResponse.success(result);
    }

    @PostMapping("/indices/{indexName}/advanced-search")
    public ApiResponse<SearchResult> advancedSearch(
            @PathVariable String indexName,
            @RequestBody AdvancedSearchRequest request) {
        SearchResult result = elasticsearchAdminService.advancedSearch(indexName, request);
        return ApiResponse.success(result);
    }

    @GetMapping("/indices/{indexName}/fields")
    public ApiResponse<List<FieldInfo>> getIndexFields(@PathVariable String indexName) {
        List<FieldInfo> fields = elasticsearchAdminService.getIndexFields(indexName);
        return ApiResponse.success(fields);
    }

    @GetMapping("/cluster/health")
    public Map<String, Object> getClusterHealth() {
        // 实现集群健康状态获取
        Map<String, Object> result = new HashMap<>();
        // ...
        return result;
    }

    @PostMapping("/create-index")
    public Map<String, Object> createCustomIndex(
            @RequestParam String indexName,
            @RequestBody String indexJson) {
        // 实现索引创建
        Map<String, Object> result = new HashMap<>();
        // ...
        return result;
    }

    @DeleteMapping("/delete-index")
    public Map<String, Object> deleteCustomIndex(@RequestParam String indexName) {
        // 实现索引删除
        Map<String, Object> result = new HashMap<>();
        // ...
        return result;
    }

    @PostMapping("/vector-store-custom-index")
    public Map<String, Object> generateAndStoreVectorToCustomIndex(
            @RequestParam String indexName,
            @RequestBody Map<String, Object> request) {
        // 实现向量生成和存储
        Map<String, Object> result = new HashMap<>();
        // ...
        return result;
    }

    @PostMapping("/custom-vector-search")
    public Map<String, Object> customVectorSearch(
            @RequestParam String indexName,
            @RequestBody Map<String, Object> request) {
        // 实现向量相似度搜索
        Map<String, Object> result = new HashMap<>();
        // ...
        return result;
    }

    @PostMapping("/custom-text-search")
    public Map<String, Object> customTextSearch(
            @RequestParam String indexName,
            @RequestBody Map<String, Object> request) {
        // 实现全文检索
        Map<String, Object> result = new HashMap<>();
        // ...
        return result;
    }
}
```

### 5.3 服务接口定义

**ElasticsearchAdminService.java**
```java
public interface ElasticsearchAdminService {
    List<IndexInfo> getAllIndices();
    IndexInfo getIndexInfo(String indexName);
    SearchResult searchIndex(String indexName, SearchRequest request);
    SearchResult advancedSearch(String indexName, AdvancedSearchRequest request);
    List<FieldInfo> getIndexFields(String indexName);
}

**ElasticsearchService.java**
```java
public interface ElasticsearchService {
    boolean createCustomIndex(String indexName, String indexJson) throws IOException;
    boolean deleteCustomIndex(String indexName) throws IOException;
    String saveDocumentToIndex(String indexName, Map<String, Object> document) throws IOException;
    List<Map<String, Object>> searchSimilarDocumentsInIndex(String indexName, float[] vector, int size) throws IOException;
    List<Map<String, Object>> searchDocumentsByText(String indexName, String queryText, List<String> fields, int size) throws IOException;
    Map<String, Object> executeDslQuery(String endpoint, String method, String dslQuery) throws IOException;
}
```

## 6. 部署说明

### 6.1 环境要求

- JDK 1.8+
- Spring Boot 2.7.x
- Elasticsearch 7.10.0+

### 6.2 配置说明

在 `application.properties` 或 `application.yml` 中配置 Elasticsearch 连接信息：

```properties
# Elasticsearch配置
elasticsearch.host=localhost
elasticsearch.port=9200
elasticsearch.username=
elasticsearch.password=
elasticsearch.connectTimeout=5000
elasticsearch.socketTimeout=60000
elasticsearch.connectionRequestTimeout=5000
elasticsearch.maxConnTotal=100
elasticsearch.maxConnPerRoute=100
```

### 6.3 启动应用

```bash
java -jar elasticsearch-admin.jar
```

## 7. 安全建议

1. 在生产环境中，建议启用 Elasticsearch 的安全功能，并配置用户名和密码
2. 对 API 接口进行访问控制，只允许授权用户访问
3. 限制查询的数据量，避免大量数据查询导致性能问题
4. 对敏感数据进行脱敏处理
5. 添加请求频率限制，防止恶意请求
6. 记录操作日志，便于审计和问题排查 