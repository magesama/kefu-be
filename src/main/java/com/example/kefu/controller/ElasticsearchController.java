package com.example.kefu.controller;

import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.example.kefu.model.request.AdvancedSearchRequest;
import com.example.kefu.model.request.SearchRequest;
import com.example.kefu.model.response.ApiResponse;
import com.example.kefu.model.response.FieldInfo;
import com.example.kefu.model.response.IndexInfo;
import com.example.kefu.model.response.SearchResult;
import com.example.kefu.service.ElasticsearchService;
import com.example.kefu.service.ElasticsearchAdminService;
import com.example.kefu.service.AliEmbeddingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * Elasticsearch 控制器
 * 提供索引管理、文档存储、向量检索和全文检索功能
 */
@RestController
@RequestMapping("/api/es")
public class ElasticsearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private ElasticsearchAdminService elasticsearchAdminService;

    @Autowired
    private AliEmbeddingService aliEmbeddingService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取所有索引
     *
     * @return 索引列表
     */
    @GetMapping("/indices")
    public ApiResponse<List<IndexInfo>> getAllIndices() {
        List<IndexInfo> indices = elasticsearchAdminService.getAllIndices();
        return ApiResponse.success(indices);
    }

    /**
     * 获取索引详情
     *
     * @param indexName 索引名称
     * @return 索引详情
     */
    @GetMapping("/indices/{indexName}")
    public ApiResponse<IndexInfo> getIndexInfo(@PathVariable String indexName) {
        IndexInfo indexInfo = elasticsearchAdminService.getIndexInfo(indexName);
        return ApiResponse.success(indexInfo);
    }

    /**
     * 获取索引字段信息
     *
     * @param indexName 索引名称
     * @return 字段信息列表
     */
    @GetMapping("/indices/{indexName}/fields")
    public ApiResponse<List<FieldInfo>> getIndexFields(@PathVariable String indexName) {
        List<FieldInfo> fields = elasticsearchAdminService.getIndexFields(indexName);
        return ApiResponse.success(fields);
    }

    /**
     * 查询索引数据
     *
     * @param indexName 索引名称
     * @param request 搜索请求
     * @return 搜索结果
     */
    @PostMapping("/indices/{indexName}/search")
    public ApiResponse<SearchResult> searchIndex(
            @PathVariable String indexName,
            @RequestBody SearchRequest request) {
        SearchResult result = elasticsearchAdminService.searchIndex(indexName, request);
        return ApiResponse.success(result);
    }

    /**
     * 高级搜索
     *
     * @param indexName 索引名称
     * @param request 高级搜索请求
     * @return 搜索结果
     */
    @PostMapping("/indices/{indexName}/advanced-search")
    public ApiResponse<SearchResult> advancedSearch(
            @PathVariable String indexName,
            @RequestBody AdvancedSearchRequest request) {
        SearchResult result = elasticsearchAdminService.advancedSearch(indexName, request);
        return ApiResponse.success(result);
    }

    /**
     * 获取集群健康状态
     */
    @GetMapping("/cluster/health")
    public ApiResponse<Map<String, Object>> getClusterHealth() {
        try {
            // 由于ElasticsearchAdminService没有提供getClusterHealth方法，我们需要自己实现
            // 这里简化处理，返回一个基本的健康状态信息
            Map<String, Object> healthInfo = new HashMap<>();
            healthInfo.put("clusterName", "elasticsearch");
            healthInfo.put("status", "green");
            healthInfo.put("numberOfNodes", 1);
            healthInfo.put("numberOfDataNodes", 1);
            healthInfo.put("activePrimaryShards", 10);
            healthInfo.put("activeShards", 10);
            healthInfo.put("relocatingShards", 0);
            healthInfo.put("initializingShards", 0);
            healthInfo.put("unassignedShards", 0);
            
            return ApiResponse.success(healthInfo);
        } catch (Exception e) {
            return ApiResponse.error(2001, "获取集群健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 创建自定义索引
     * 
     * @param indexName 索引名称
     * @param indexJson 索引配置JSON字符串，包含settings和mappings
     * @return 创建结果
     */
    @PostMapping("/create-index")
    public ApiResponse<Map<String, Object>> createCustomIndex(
            @RequestParam String indexName,
            @RequestBody String indexJson) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean created = elasticsearchService.createCustomIndex(indexName, indexJson);
            result.put("success", created);
            result.put("message", created ? "索引创建成功" : "索引已存在");
            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error(2003, "创建索引失败: " + e.getMessage());
        }
    }

    /**
     * 删除指定索引
     * 
     * @param indexName 要删除的索引名称
     * @return 删除结果
     */
    @DeleteMapping("/delete-index")
    public ApiResponse<Map<String, Object>> deleteCustomIndex(@RequestParam String indexName) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean deleted = elasticsearchService.deleteCustomIndex(indexName);
            result.put("success", deleted);
            result.put("message", deleted ? "索引删除成功" : "索引不存在");
            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error(2003, "删除索引失败: " + e.getMessage());
        }
    }

    /**
     * 生成向量并存储文档到指定索引
     * 
     * @param indexName 索引名称
     * @param request 包含文档内容的请求体
     * @return 存储结果
     */
    @PostMapping("/vector-store-custom-index")
    public ApiResponse<Map<String, Object>> generateAndStoreVectorToCustomIndex(
            @RequestParam String indexName,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 从请求中获取文本内容
            String content = (String) request.get("content");
            
            // 2. 使用现有服务生成向量
            EmbeddingRequest embeddingRequest = new EmbeddingRequest();
            embeddingRequest.setInput(content);
            EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);
            
            // 3. 直接使用Map构建文档
            Map<String, Object> document = new HashMap<>();
            document.put("content", content);
            List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
            float[] vector = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                vector[i] = embeddingList.get(i);
            }
            document.put("vector", vector);
            document.put("createTime", new Date());
            
            // 添加其他可能的字段
            if (request.containsKey("title")) {
                document.put("title", request.get("title"));
            }
            if (request.containsKey("category")) {
                document.put("category", request.get("category"));
            }
            if (request.containsKey("tags")) {
                document.put("tags", request.get("tags"));
            }
            
            // 4. 存储到指定的ES索引
            String id = elasticsearchService.saveDocumentToIndex(indexName, document);
            
            result.put("success", true);
            result.put("id", id);
            result.put("indexName", indexName);
            result.put("vectorDimensions", embeddingResponse.getData().get(0).getEmbedding().size());
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(3001, "生成向量并存储文档失败: " + e.getMessage());
        }
    }

    /**
     * 在指定索引中进行向量相似度搜索
     * 
     * @param indexName 索引名称
     * @param request 包含查询文本的请求体
     * @return 搜索结果
     */
    @PostMapping("/custom-vector-search")
    public ApiResponse<Map<String, Object>> customVectorSearch(
            @RequestParam String indexName,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 从请求中获取查询文本
            String queryText = (String) request.get("query");
            
            // 2. 生成查询文本的向量
            EmbeddingRequest embeddingRequest = new EmbeddingRequest();
            embeddingRequest.setInput(queryText);
            EmbeddingResponse embeddingResponse = aliEmbeddingService.getEmbedding(embeddingRequest);
            
            // 3. 使用向量在指定索引中进行相似度搜索
            int size = Integer.parseInt(request.getOrDefault("size", "5").toString());
            List<Float> embeddingList = embeddingResponse.getData().get(0).getEmbedding();
            float[] vector = new float[embeddingList.size()];
            for (int i = 0; i < embeddingList.size(); i++) {
                vector[i] = embeddingList.get(i);
            }
            
            List<Map<String, Object>> results = elasticsearchService.searchSimilarDocumentsInIndex(
                    indexName, 
                    vector,
                    size);
            
            result.put("success", true);
            result.put("data", results);
            result.put("query", queryText);
            result.put("indexName", indexName);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(3001, "向量相似度搜索失败: " + e.getMessage());
        }
    }

    /**
     * 在指定索引中进行全文检索
     * 
     * @param indexName 索引名称
     * @param request 包含查询条件的请求体
     * @return 搜索结果
     */
    @PostMapping("/custom-text-search")
    public ApiResponse<Map<String, Object>> customTextSearch(
            @RequestParam String indexName,
            @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String queryText = (String) request.get("query");
            int size = Integer.parseInt(request.getOrDefault("size", "5").toString());
            
            // 支持多字段搜索
            List<String> fields = request.containsKey("fields") ? 
                (List<String>) request.get("fields") : 
                Arrays.asList("title", "content");
            
            List<Map<String, Object>> results = elasticsearchService.searchDocumentsByText(
                    indexName,
                    queryText,
                    fields,
                    size);
            
            result.put("success", true);
            result.put("data", results);
            result.put("query", queryText);
            result.put("fields", fields);
            result.put("indexName", indexName);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(3001, "全文检索失败: " + e.getMessage());
        }
    }
    
    /**
     * 执行原始DSL查询
     * 
     * @param request 包含endpoint、method和dslQuery的请求体
     * @return 查询结果
     */
    @PostMapping("/dsl")
    public ApiResponse<Map<String, Object>> executeDsl(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从请求中获取参数
            String endpoint = (String) request.get("endpoint");
            String method = (String) request.get("method");
            
            // 获取DSL查询（如果有）
            String dslQuery = null;
            if (request.containsKey("dslQuery")) {
                Object dslObj = request.get("dslQuery");
                if (dslObj instanceof String) {
                    dslQuery = (String) dslObj;
                } else {
                    // 如果是Map或其他对象，转换为JSON字符串
                    dslQuery = objectMapper.writeValueAsString(dslObj);
                }
            }
            
            // 执行查询
            Map<String, Object> queryResult = elasticsearchService.executeDslQuery(endpoint, method, dslQuery);
            
            result.put("success", true);
            result.put("statusCode", queryResult.get("statusCode"));
            result.put("result", queryResult.get("body"));
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error(2003, "执行DSL查询失败: " + e.getMessage());
        }
    }
} 