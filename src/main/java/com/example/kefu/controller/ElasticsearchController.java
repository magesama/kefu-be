package com.example.kefu.controller;

import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.example.kefu.service.ElasticsearchService;
import com.example.kefu.service.AliEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * Elasticsearch 控制器
 * 提供索引创建、文档存储、向量检索和全文检索功能
 */
@RestController
@RequestMapping("/api/es")
public class ElasticsearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private AliEmbeddingService aliEmbeddingService;

    /**
     * 创建自定义索引
     * 
     * @param indexName 索引名称
     * @param mappingJson 索引映射JSON字符串
     * @return 创建结果
     * 
     * 示例请求：
     * POST /api/es/create-index?indexName=test_vectors
     * 请求体：
     * {
     *   "properties": {
     *     "title": {
     *       "type": "text",
     *       "analyzer": "smartcn"
     *     },
     *     "content": {
     *       "type": "text",
     *       "analyzer": "smartcn"
     *     },
     *     "vector": {
     *       "type": "dense_vector",
     *       "dims": 512
     *     }
     *   }
     * }
     * 
     * 响应：
     * {
     *   "success": true,
     *   "message": "索引创建成功"
     * }
     */
    @PostMapping("/create-index")
    public Map<String, Object> createCustomIndex(
            @RequestParam String indexName,
            @RequestBody String mappingJson) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean created = elasticsearchService.createCustomIndex(indexName, mappingJson);
            result.put("success", true);
            result.put("message", created ? "索引创建成功" : "索引已存在");
        } catch (IOException e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 生成向量并存储文档到指定索引
     * 
     * @param indexName 索引名称
     * @param request 包含文档内容的请求体
     * @return 存储结果
     * 
     * 示例请求：
     * POST /api/es/vector-store-custom-index?indexName=test_vectors
     * 请求体：
     * {
     *   "content": "如何申请退款？",
     *   "title": "退款申请流程",
     *   "category": "退款相关",
     *   "tags": ["退款", "售后"]
     * }
     * 
     * 响应：
     * {
     *   "success": true,
     *   "id": "xxx",
     *   "indexName": "test_vectors",
     *   "vectorDimensions": 512
     * }
     */
    @PostMapping("/vector-store-custom-index")
    public Map<String, Object> generateAndStoreVectorToCustomIndex(
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 在指定索引中进行向量相似度搜索
     * 
     * @param indexName 索引名称
     * @param request 包含查询文本的请求体
     * @return 搜索结果
     * 
     * 示例请求：
     * POST /api/es/custom-vector-search?indexName=test_vectors
     * 请求体：
     * {
     *   "query": "如何退款？商品不满意",
     *   "size": 5
     * }
     * 
     * 响应：
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "_id": "xxx",
     *       "_score": 0.95,
     *       "title": "退款申请流程",
     *       "content": "如何申请退款？",
     *       ...
     *     }
     *   ],
     *   "query": "如何退款？商品不满意",
     *   "indexName": "test_vectors"
     * }
     */
    @PostMapping("/custom-vector-search")
    public Map<String, Object> customVectorSearch(
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /**
     * 在指定索引中进行全文检索
     * 
     * @param indexName 索引名称
     * @param request 包含查询条件的请求体
     * @return 搜索结果
     * 
     * 示例请求：
     * POST /api/es/custom-text-search?indexName=test_vectors
     * 请求体：
     * {
     *   "query": "退款",
     *   "fields": ["title", "content"],
     *   "size": 5
     * }
     * 
     * 响应：
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "_id": "xxx",
     *       "_score": 0.8,
     *       "title": "退款申请流程",
     *       "content": "如何申请退款？",
     *       ...
     *     }
     *   ],
     *   "query": "退款",
     *   "fields": ["title", "content"],
     *   "indexName": "test_vectors"
     * }
     */
    @PostMapping("/custom-text-search")
    public Map<String, Object> customTextSearch(
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
} 