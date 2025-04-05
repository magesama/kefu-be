package com.example.kefu.service;

import com.example.kefu.entity.es.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScriptScoreQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import com.example.kefu.model.response.SearchResult;

@Service
public class ElasticsearchService {

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String INDEX_NAME = "chat_messages";

    /**
     * 创建索引
     */
    public boolean createIndex() throws IOException {
        if (indexExists()) {
            return false;
        }

        CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 1)
                .put("index.number_of_replicas", 0)
                .build());

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("content");
                builder.field("type", "text");
                builder.field("analyzer", "smartcn");
                builder.field("search_analyzer", "smartcn");
                builder.endObject();

                builder.startObject("userId");
                builder.field("type", "keyword");
                builder.endObject();

                builder.startObject("sessionId");
                builder.field("type", "keyword");
                builder.endObject();

                builder.startObject("vector");
                builder.field("type", "dense_vector");
                builder.field("dims", 384);
                builder.endObject();

                builder.startObject("createTime");
                builder.field("type", "date");
                builder.endObject();

                builder.startObject("messageType");
                builder.field("type", "integer");
                builder.endObject();
            }
            builder.endObject();
        }
        builder.endObject();
        request.source(builder);

        client.indices().create(request, RequestOptions.DEFAULT);
        return true;
    }

    /**
     * 检查索引是否存在
     */
    public boolean indexExists() throws IOException {
        GetIndexRequest request = new GetIndexRequest(INDEX_NAME);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 删除索引
     */
    public boolean deleteIndex() throws IOException {
        if (!indexExists()) {
            return false;
        }
        DeleteIndexRequest request = new DeleteIndexRequest(INDEX_NAME);
        client.indices().delete(request, RequestOptions.DEFAULT);
        return true;
    }

    /**
     * 保存聊天消息
     */
    public String saveMessage(ChatMessage message) throws IOException {
        IndexRequest request = new IndexRequest(INDEX_NAME);
        request.source(objectMapper.writeValueAsString(message), XContentType.JSON);
        return client.index(request, RequestOptions.DEFAULT).getId();
    }

    /**
     * 全文搜索
     */
    public List<ChatMessage> searchMessages(String keyword, String userId, int from, int size) throws IOException {
        SearchRequest request = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (keyword != null && !keyword.isEmpty()) {
            boolQuery.must(QueryBuilders.matchQuery("content", keyword));
        }
        if (userId != null && !userId.isEmpty()) {
            boolQuery.must(QueryBuilders.termQuery("userId", userId));
        }

        sourceBuilder.query(boolQuery);
        sourceBuilder.from(from);
        sourceBuilder.size(size);
        sourceBuilder.sort("createTime", SortOrder.DESC);

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        List<ChatMessage> messages = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            ChatMessage message = objectMapper.readValue(hit.getSourceAsString(), ChatMessage.class);
            message.setId(hit.getId());
            messages.add(message);
        }

        return messages;
    }

    /**
     * 向量相似度搜索
     */
    public List<ChatMessage> searchSimilarMessages(float[] queryVector, int size) throws IOException {
        SearchRequest request = new SearchRequest(INDEX_NAME);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        Map<String, Object> params = new HashMap<>();
        params.put("query_vector", queryVector);

        Script script = new Script(
                ScriptType.INLINE,
                "painless",
                "double dotProduct = 0.0; " +
                "for (int i = 0; i < params.query_vector.length; i++) { " +
                "    dotProduct += params.query_vector[i] * doc['vector'][i]; " +
                "} " +
                "double magnitude = 0.0; " +
                "for (int i = 0; i < doc['vector'].length; i++) { " +
                "    magnitude += doc['vector'][i] * doc['vector'][i]; " +
                "} " +
                "double queryMagnitude = 0.0; " +
                "for (int i = 0; i < params.query_vector.length; i++) { " +
                "    queryMagnitude += params.query_vector[i] * params.query_vector[i]; " +
                "} " +
                "return dotProduct / Math.sqrt(magnitude * queryMagnitude);",
                params
        );

        ScriptScoreFunctionBuilder scoreFunction = new ScriptScoreFunctionBuilder(script);
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(scoreFunction);

        sourceBuilder.query(functionScoreQuery);
        sourceBuilder.size(size);

        request.source(sourceBuilder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        List<ChatMessage> messages = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            ChatMessage message = objectMapper.readValue(hit.getSourceAsString(), ChatMessage.class);
            message.setId(hit.getId());
            messages.add(message);
        }

        return messages;
    }

    /**
     * 创建自定义索引
     *
     * @param indexName 索引名称
     * @param indexJson 包含settings和mappings的完整索引配置JSON
     * @return 是否成功创建（如果已存在则返回false）
     * @throws IOException 如果创建过程中发生IO异常
     */
    public boolean createCustomIndex(String indexName, String indexJson) throws IOException {
        // 检查索引是否已存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

        if (exists) {
            return false;
        }

        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        
        // 设置完整的索引配置（包含settings和mappings）
        request.source(indexJson, XContentType.JSON);

        // 执行创建索引
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        // 返回是否创建成功
        return createIndexResponse.isAcknowledged();
    }

    /**
     * 将文档保存到指定的索引
     * @param indexName 索引名称
     * @param document 文档内容
     * @return 文档ID
     */
    public String saveDocumentToIndex(String indexName, Map<String, Object> document) throws IOException {
        IndexRequest request = new IndexRequest(indexName);
        request.source(document);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        return response.getId();
    }

    /**
     * 在指定索引中搜索相似文档
     * @param indexName 索引名称
     * @param vector 查询向量
     * @param size 返回结果数量
     * @return 相似文档列表
     */
    public List<Map<String, Object>> searchSimilarDocumentsInIndex(String indexName, float[] vector, int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // 使用match_all查询，不使用向量搜索
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> resultMap = new HashMap<>(hit.getSourceAsMap());
            resultMap.put("_id", hit.getId());
            resultMap.put("_score", hit.getScore());
            results.add(resultMap);
        }
        
        return results;
    }

    /**
     * 在指定索引中进行全文检索
     * @param indexName 索引名称
     * @param queryText 查询文本
     * @param fields 要搜索的字段列表
     * @param size 返回结果数量
     * @return 搜索结果列表
     */
    public List<Map<String, Object>> searchDocumentsByText(String indexName, String queryText, List<String> fields, int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        
        // 构建多字段查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        
        // 对每个字段构建模糊查询
        for (String field : fields) {
            boolQuery.should(QueryBuilders.matchQuery(field, queryText));
        }
        
        searchSourceBuilder.query(boolQuery);
        searchSourceBuilder.size(size);
        
        // 按相关度分数排序
        searchSourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
        
        searchRequest.source(searchSourceBuilder);
        
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        
        List<Map<String, Object>> results = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Map<String, Object> resultMap = new HashMap<>(hit.getSourceAsMap());
            resultMap.put("_id", hit.getId());
            resultMap.put("_score", hit.getScore());
            results.add(resultMap);
        }
        
        return results;
    }

    /**
     * 删除指定索引
     *
     * @param indexName 要删除的索引名称
     * @return 是否成功删除（如果不存在则返回false）
     * @throws IOException 如果删除过程中发生IO异常
     */
    public boolean deleteCustomIndex(String indexName) throws IOException {
        try {
            // 检查索引是否存在
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            // 设置更长的超时时间（使用正确的方法）
            getIndexRequest.setTimeout(org.elasticsearch.common.unit.TimeValue.timeValueSeconds(30));
            
            boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

            if (!exists) {
                return false;
            }

            // 创建删除索引请求
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            // 设置更长的超时时间（使用正确的方法）
            request.timeout(org.elasticsearch.common.unit.TimeValue.timeValueSeconds(30));
            
            // 执行删除索引
            client.indices().delete(request, RequestOptions.DEFAULT);
            
            // 返回删除成功
            return true;
        } catch (IOException e) {
            System.err.println("删除索引时发生错误: " + indexName);
            System.err.println("错误消息: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 执行原始DSL查询
     * 
     * @param endpoint Elasticsearch API端点，例如 "_search", "my_index/_search", "_cat/indices" 等
     * @param method HTTP方法，例如 "GET", "POST", "PUT", "DELETE"
     * @param dslQuery DSL查询JSON字符串
     * @return 查询结果
     * @throws IOException 如果查询过程中发生IO异常
     */
    public Map<String, Object> executeDslQuery(String endpoint, String method, String dslQuery) throws IOException {
        // 创建低级客户端请求
        org.elasticsearch.client.Request request = new org.elasticsearch.client.Request(
            method,
            "/" + endpoint
        );
        
        // 如果有查询体，则设置
        if (dslQuery != null && !dslQuery.trim().isEmpty()) {
            request.setJsonEntity(dslQuery);
        }
        
        // 执行请求
        org.elasticsearch.client.Response response = client.getLowLevelClient().performRequest(request);
        
        // 解析响应
        Map<String, Object> result = new HashMap<>();
        
        // 获取状态码
        result.put("statusCode", response.getStatusLine().getStatusCode());
        
        // 获取响应体
        String responseBody;
        try (java.io.InputStream is = response.getEntity().getContent();
             java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, java.nio.charset.StandardCharsets.UTF_8))) {
            
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            responseBody = sb.toString();
        }
        
        // 尝试将响应体解析为JSON
        try {
            result.put("body", objectMapper.readValue(responseBody, Map.class));
        } catch (Exception e) {
            // 如果无法解析为JSON，则作为字符串返回
            result.put("body", responseBody);
        }
        
        return result;
    }

    /**
     * 向量相似度搜索
     *
     * @param vector 查询向量
     * @param topK 返回结果数量
     * @param threshold 相似度阈值
     * @return 搜索结果列表
     */
    public List<SearchResult> searchByVector(float[] vector, int topK, float threshold) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 全文检索搜索
     *
     * @param text 查询文本
     * @param topK 返回结果数量
     * @param threshold 相似度分数阈值
     * @return 搜索结果列表
     */
    public List<SearchResult> searchByText(String text, int topK, float threshold) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }

    /**
     * 构建精确匹配条件（term查询）
     * 
     * 示例：
     * Map<String, Object> termQuery = buildTermQuery("userId", 123);
     * 生成：{"term": {"userId": 123}}
     * 
     * @param field 字段名
     * @param value 字段值
     * @return term查询条件
     */
    public Map<String, Object> buildTermQuery(String field, Object value) {
        Map<String, Object> termValue = new HashMap<>();
        termValue.put(field, value);
        
        Map<String, Object> term = new HashMap<>();
        term.put("term", termValue);
        
        return term;
    }
    
    /**
     * 构建多条件查询（bool查询）
     * 
     * 示例：
     * List<Map<String, Object>> mustConditions = new ArrayList<>();
     * mustConditions.add(buildTermQuery("userId", 123));
     * mustConditions.add(buildTermQuery("shopName", "某店铺"));
     * Map<String, Object> boolQuery = buildBoolQuery(mustConditions, null, null);
     * 生成：{"bool": {"must": [{"term": {"userId": 123}}, {"term": {"shopName": "某店铺"}}]}}
     * 
     * @param must 必须满足的条件列表
     * @param should 应该满足的条件列表（可为null）
     * @param mustNot 必须不满足的条件列表（可为null）
     * @return bool查询条件
     */
    public Map<String, Object> buildBoolQuery(List<Map<String, Object>> must, 
                                             List<Map<String, Object>> should, 
                                             List<Map<String, Object>> mustNot) {
        Map<String, Object> bool = new HashMap<>();
        
        if (must != null && !must.isEmpty()) {
            bool.put("must", must);
        }
        
        if (should != null && !should.isEmpty()) {
            bool.put("should", should);
        }
        
        if (mustNot != null && !mustNot.isEmpty()) {
            bool.put("must_not", mustNot);
        }
        
        Map<String, Object> query = new HashMap<>();
        query.put("bool", bool);
        
        return query;
    }
    
    /**
     * 构建向量相似度查询（script_score查询）
     * 
     * 示例：
     * Map<String, Object> baseQuery = buildBoolQuery(...);
     * float[] vector = new float[]{0.1f, 0.2f, ...};
     * Map<String, Object> scriptScoreQuery = buildVectorScriptQuery(baseQuery, vector, "vector");
     * 生成：{
     *   "function_score": {
     *     "query": {...},
     *     "script_score": {
     *       "script": {
     *         "source": "cosineSimilarity(params.query_vector, 'vector') + 1.0",
     *         "params": {"query_vector": [0.1, 0.2, ...]}
     *       }
     *     }
     *   }
     * }
     * 
     * @param query 基础查询条件
     * @param vector 查询向量
     * @param vectorField 向量字段名
     * @return script_score查询条件
     */
    public Map<String, Object> buildVectorScriptQuery(Map<String, Object> query, float[] vector, String vectorField) {
        // 构建script部分
        Map<String, Object> script = new HashMap<>();
        script.put("source", "cosineSimilarity(params.query_vector, '" + vectorField + "') + 1.0");
        
        Map<String, Object> params = new HashMap<>();
        params.put("query_vector", vector);
        script.put("params", params);
        
        // 构建script_score部分
        Map<String, Object> scriptScore = new HashMap<>();
        scriptScore.put("script", script);
        
        // 构建function_score部分
        Map<String, Object> functionScore = new HashMap<>();
        functionScore.put("query", query);
        functionScore.put("script_score", scriptScore);
        
        // 构建最终查询
        Map<String, Object> result = new HashMap<>();
        result.put("function_score", functionScore);
        
        return result;
    }
    
    /**
     * 构建完整的DSL查询（支持指定返回字段）
     * 
     * 示例：
     * List<String> includeFields = Arrays.asList("question", "answer", "productName");
     * List<String> excludeFields = Arrays.asList("vector");
     * Map<String, Object> dslQuery = buildDslQuery(query, 10, "createTime", "desc", includeFields, excludeFields);
     * 生成：{
     *   "query": {...},
     *   "size": 10,
     *   "sort": [{"createTime": {"order": "desc"}}],
     *   "_source": {
     *     "includes": ["question", "answer", "productName"],
     *     "excludes": ["vector"]
     *   }
     * }
     * 
     * @param query 查询条件
     * @param size 返回结果数量
     * @param sortField 排序字段（可为null）
     * @param sortOrder 排序顺序（"asc"或"desc"，可为null）
     * @param includeFields 要包含的字段列表（可为null）
     * @param excludeFields 要排除的字段列表（可为null）
     * @return 完整的DSL查询
     */
    public Map<String, Object> buildDslQuery(Map<String, Object> query, int size, 
                                            String sortField, String sortOrder,
                                            List<String> includeFields, List<String> excludeFields) {
        Map<String, Object> dslQuery = new HashMap<>();
        dslQuery.put("query", query);
        dslQuery.put("size", size);
        
        // 添加排序（如果指定了排序字段）
        if (sortField != null && !sortField.isEmpty()) {
            List<Map<String, Object>> sort = new ArrayList<>();
            Map<String, Object> sortItem = new HashMap<>();
            Map<String, Object> order = new HashMap<>();
            order.put("order", sortOrder != null ? sortOrder : "desc");
            sortItem.put(sortField, order);
            sort.add(sortItem);
            dslQuery.put("sort", sort);
        }
        
        // 添加字段过滤
        if ((includeFields != null && !includeFields.isEmpty()) || 
            (excludeFields != null && !excludeFields.isEmpty())) {
            Map<String, Object> source = new HashMap<>();
            
            if (includeFields != null && !includeFields.isEmpty()) {
                source.put("includes", includeFields);
            }
            
            if (excludeFields != null && !excludeFields.isEmpty()) {
                source.put("excludes", excludeFields);
            }
            
            dslQuery.put("_source", source);
        }
        
        return dslQuery;
    }
    
    /**
     * 重载原有的buildDslQuery方法，保持向后兼容
     */
    public Map<String, Object> buildDslQuery(Map<String, Object> query, int size, 
                                            String sortField, String sortOrder) {
        return buildDslQuery(query, size, sortField, sortOrder, null, null);
    }
    
    /**
     * 将对象转换为JSON字符串
     * 
     * @param object 要转换的对象
     * @return JSON字符串
     * @throws IOException 如果转换失败
     */
    public String convertToJson(Object object) throws IOException {
        return objectMapper.writeValueAsString(object);
    }
    
    /**
     * 执行向量搜索（支持自定义返回字段）
     * 
     * @param indexName 索引名称
     * @param userId 用户ID
     * @param shopName 店铺名称（可为null）
     * @param productName 产品名称（可为null）
     * @param vector 查询向量
     * @param size 返回结果数量
     * @param threshold 相似度阈值
     * @param includeFields 要包含的字段列表（可为null）
     * @param excludeFields 要排除的字段列表（可为null）
     * @param vectorField 向量字段名称，默认为"question_vector"
     * @return 查询结果
     * @throws IOException 如果查询失败
     */
    public Map<String, Object> searchByVector(String indexName, Long userId, String shopName, 
                                             String productName, float[] vector, int size, 
                                             float threshold, List<String> includeFields, 
                                             List<String> excludeFields, String vectorField) throws IOException {
        // 构建必须满足的条件
        List<Map<String, Object>> mustConditions = new ArrayList<>();
        
        // 用户ID精确匹配
        mustConditions.add(buildTermQuery("userId", userId));
        
        // 如果提供了店铺名称，添加店铺名称精确匹配
        if (shopName != null && !shopName.trim().isEmpty()) {
            mustConditions.add(buildTermQuery("shopName", shopName));
        }
        
        // 如果提供了产品名称，添加产品名称模糊匹配（而不是精确匹配）
        if (productName != null && !productName.trim().isEmpty()) {
            mustConditions.add(buildMatchQuery("productName", productName));
        }
        
        // 构建bool查询
        Map<String, Object> boolQuery = buildBoolQuery(mustConditions, null, null);
        
        // 构建向量脚本查询
        Map<String, Object> vectorQuery = buildVectorScriptQuery(boolQuery, vector, vectorField);
        
        // 构建完整的DSL查询
        Map<String, Object> dslQuery = buildDslQuery(vectorQuery, size, null, null, includeFields, excludeFields);
        
        // 执行查询
        String dslQueryJson = convertToJson(dslQuery);
        return executeDslQuery(indexName + "/_search", "GET", dslQueryJson);
    }
    
    /**
     * 重载原有的searchByVector方法，保持向后兼容
     */
    public Map<String, Object> searchByVector(String indexName, Long userId, String shopName, 
                                             String productName, float[] vector, int size, 
                                             float threshold, List<String> includeFields, 
                                             List<String> excludeFields) throws IOException {
        // 默认使用question_vector字段
        return searchByVector(indexName, userId, shopName, productName, vector, size, threshold, 
                             includeFields, excludeFields, "question_vector");
    }
    
    /**
     * 重载原有的searchByVector方法，保持向后兼容
     */
    public Map<String, Object> searchByVector(String indexName, Long userId, String shopName, 
                                             String productName, float[] vector, int size, 
                                             float threshold) throws IOException {
        // 默认排除向量字段，使用question_vector字段
        return searchByVector(indexName, userId, shopName, productName, vector, size, threshold, 
                             null, Arrays.asList("question_vector", "answer_vector"), "question_vector");
    }

    /**
     * 执行全文检索模糊匹配（支持自定义返回字段）
     * 
     * @param indexName 索引名称
     * @param queryText 查询文本
     * @param fields 要搜索的字段列表
     * @param userId 用户ID（可为null）
     * @param shopName 店铺名称（可为null）
     * @param productName 产品名称（可为null）
     * @param size 返回结果数量
     * @param includeFields 要包含的字段列表（可为null）
     * @param excludeFields 要排除的字段列表（可为null）
     * @return 查询结果
     * @throws IOException 如果查询失败
     */
    public Map<String, Object> searchByText(String indexName, String queryText, List<String> fields,
                                          Long userId, String shopName, String productName, 
                                          int size, List<String> includeFields, 
                                          List<String> excludeFields) throws IOException {
        // 构建必须满足的条件
        List<Map<String, Object>> mustConditions = new ArrayList<>();
        
        // 添加多字段模糊匹配查询
        mustConditions.add(buildMultiMatchQuery(fields, queryText, "best_fields", 0.3f));
        
        // 如果提供了用户ID，添加用户ID精确匹配
        if (userId != null) {
            mustConditions.add(buildTermQuery("userId", userId));
        }
        
        // 如果提供了店铺名称，添加店铺名称精确匹配
        if (shopName != null && !shopName.trim().isEmpty()) {
            mustConditions.add(buildTermQuery("shopName", shopName));
        }
        
        // 如果提供了产品名称，添加产品名称模糊匹配（而不是精确匹配）
        if (productName != null && !productName.trim().isEmpty()) {
            mustConditions.add(buildMatchQuery("productName", productName));
        }
        
        // 构建bool查询
        Map<String, Object> boolQuery = buildBoolQuery(mustConditions, null, null);
        
        // 构建完整的DSL查询
        Map<String, Object> dslQuery = buildDslQuery(boolQuery, size, "_score", "desc", includeFields, excludeFields);
        
        // 执行查询
        String dslQueryJson = convertToJson(dslQuery);
        return executeDslQuery(indexName + "/_search", "GET", dslQueryJson);
    }
    
    /**
     * 重载原有的searchByText方法，保持向后兼容
     */
    public Map<String, Object> searchByText(String indexName, String queryText, List<String> fields,
                                          Long userId, String shopName, String productName, 
                                          int size) throws IOException {
        // 默认排除向量字段
        return searchByText(indexName, queryText, fields, userId, shopName, productName, size,
                           null, Arrays.asList("question_vector", "answer_vector"));
    }
    
    /**
     * 执行混合搜索（结合全文检索和向量搜索，支持自定义返回字段）
     * 
     * @param indexName 索引名称
     * @param queryText 查询文本
     * @param fields 要搜索的字段列表
     * @param vector 查询向量
     * @param textWeight 文本搜索权重（0.0-1.0）
     * @param vectorWeight 向量搜索权重（0.0-1.0）
     * @param userId 用户ID（可为null）
     * @param shopName 店铺名称（可为null）
     * @param productName 产品名称（可为null）
     * @param size 返回结果数量
     * @param includeFields 要包含的字段列表（可为null）
     * @param excludeFields 要排除的字段列表（可为null）
     * @param vectorField 向量字段名称，默认为"question_vector"
     * @return 查询结果
     * @throws IOException 如果查询失败
     */
    public Map<String, Object> searchHybrid(String indexName, String queryText, List<String> fields,
                                          float[] vector, float textWeight, float vectorWeight,
                                          Long userId, String shopName, String productName, 
                                          int size, List<String> includeFields, 
                                          List<String> excludeFields, String vectorField) throws IOException {
        // 构建必须满足的条件
        List<Map<String, Object>> mustConditions = new ArrayList<>();
        
        // 添加多字段模糊匹配查询
        Map<String, Object> textQuery = buildMultiMatchQuery(fields, queryText, "best_fields", textWeight);
        
        // 如果提供了用户ID，添加用户ID精确匹配
        if (userId != null) {
            mustConditions.add(buildTermQuery("userId", userId));
        }
        
        // 如果提供了店铺名称，添加店铺名称精确匹配
        if (shopName != null && !shopName.trim().isEmpty()) {
            mustConditions.add(buildTermQuery("shopName", shopName));
        }
        
        // 如果提供了产品名称，添加产品名称模糊匹配（而不是精确匹配）
        if (productName != null && !productName.trim().isEmpty()) {
            mustConditions.add(buildMatchQuery("productName", productName));
        }
        
        // 构建bool查询
        Map<String, Object> boolQuery = buildBoolQuery(mustConditions, null, null);
        
        // 构建向量脚本查询
        Map<String, Object> vectorScriptQuery = buildVectorScriptQuery(boolQuery, vector, vectorField);
        
        // 构建should条件，包含文本查询和向量查询
        List<Map<String, Object>> shouldConditions = new ArrayList<>();
        shouldConditions.add(textQuery);
        shouldConditions.add(vectorScriptQuery);
        
        // 构建最终的bool查询
        Map<String, Object> finalBoolQuery = buildBoolQuery(null, shouldConditions, null);
        
        // 构建完整的DSL查询
        Map<String, Object> dslQuery = buildDslQuery(finalBoolQuery, size, "_score", "desc", includeFields, excludeFields);
        
        // 执行查询
        String dslQueryJson = convertToJson(dslQuery);
        return executeDslQuery(indexName + "/_search", "GET", dslQueryJson);
    }
    
    /**
     * 重载原有的searchHybrid方法，保持向后兼容
     */
    public Map<String, Object> searchHybrid(String indexName, String queryText, List<String> fields,
                                          float[] vector, float textWeight, float vectorWeight,
                                          Long userId, String shopName, String productName, 
                                          int size, List<String> includeFields, 
                                          List<String> excludeFields) throws IOException {
        // 默认使用question_vector字段
        return searchHybrid(indexName, queryText, fields, vector, textWeight, vectorWeight,
                           userId, shopName, productName, size, includeFields, excludeFields, "question_vector");
    }
    
    /**
     * 重载原有的searchHybrid方法，保持向后兼容
     */
    public Map<String, Object> searchHybrid(String indexName, String queryText, List<String> fields,
                                          float[] vector, float textWeight, float vectorWeight,
                                          Long userId, String shopName, String productName, 
                                          int size) throws IOException {
        // 默认排除向量字段，使用question_vector字段
        return searchHybrid(indexName, queryText, fields, vector, textWeight, vectorWeight,
                           userId, shopName, productName, size, null, Arrays.asList("question_vector", "answer_vector"), "question_vector");
    }

    /**
     * 从查询结果中提取相似度高于阈值的文档
     * 
     * @param searchResult 查询结果
     * @param threshold 相似度阈值
     * @return 满足条件的文档列表
     */
    public List<Map<String, Object>> extractRelevantDocuments(Map<String, Object> searchResult, 
                                                             double threshold) {
        List<Map<String, Object>> relevantDocs = new ArrayList<>();
        
        if (searchResult.containsKey("body")) {
            Map<String, Object> body = (Map<String, Object>) searchResult.get("body");
            if (body.containsKey("hits")) {
                Map<String, Object> hits = (Map<String, Object>) body.get("hits");
                List<Map<String, Object>> hitsList = (List<Map<String, Object>>) hits.get("hits");
                
                for (Map<String, Object> hit : hitsList) {
                    double score = (double) hit.get("_score");
                    if (score > threshold) {
                        Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                        relevantDocs.add(source);
                    }
                }
            }
        }
        
        return relevantDocs;
    }

    /**
     * 构建模糊匹配查询（match查询）
     * 
     * 示例：
     * Map<String, Object> matchQuery = buildMatchQuery("question", "袜子材质");
     * 生成：{"match": {"question": "袜子材质"}}
     * 
     * @param field 字段名
     * @param value 查询值
     * @return match查询条件
     */
    public Map<String, Object> buildMatchQuery(String field, Object value) {
        Map<String, Object> matchValue = new HashMap<>();
        matchValue.put(field, value);
        
        Map<String, Object> match = new HashMap<>();
        match.put("match", matchValue);
        
        return match;
    }
    
    /**
     * 构建多字段模糊匹配查询（multi_match查询）
     * 
     * 示例：
     * List<String> fields = Arrays.asList("question", "answer");
     * Map<String, Object> multiMatchQuery = buildMultiMatchQuery(fields, "袜子材质", "best_fields", 1.0f);
     * 生成：{
     *   "multi_match": {
     *     "query": "袜子材质",
     *     "fields": ["question", "answer"],
     *     "type": "best_fields",
     *     "tie_breaker": 1.0
     *   }
     * }
     * 
     * @param fields 字段列表
     * @param value 查询值
     * @param type 查询类型（best_fields, most_fields, cross_fields等）
     * @param tieBreaker 打破平局的系数（0.0-1.0）
     * @return multi_match查询条件
     */
    public Map<String, Object> buildMultiMatchQuery(List<String> fields, Object value, 
                                                  String type, float tieBreaker) {
        Map<String, Object> multiMatchValue = new HashMap<>();
        multiMatchValue.put("query", value);
        multiMatchValue.put("fields", fields);
        
        if (type != null && !type.isEmpty()) {
            multiMatchValue.put("type", type);
        }
        
        if (tieBreaker >= 0 && tieBreaker <= 1.0) {
            multiMatchValue.put("tie_breaker", tieBreaker);
        }
        
        Map<String, Object> multiMatch = new HashMap<>();
        multiMatch.put("multi_match", multiMatchValue);
        
        return multiMatch;
    }

    /**
     * 根据产品ID查询问答对
     * 
     * @param indexName 索引名称
     * @param productId 产品ID
     * @param size 返回结果数量
     * @param includeFields 要包含的字段列表
     * @return 查询结果
     * @throws IOException 如果查询失败
     */
    public Map<String, Object> searchByProductId(String indexName, Long productId, int size, 
                                               List<String> includeFields) throws IOException {
        // 构建必须满足的条件
        List<Map<String, Object>> mustConditions = new ArrayList<>();
        
        // 产品ID精确匹配
        mustConditions.add(buildTermQuery("productId", productId));
        
        // 构建bool查询
        Map<String, Object> boolQuery = buildBoolQuery(mustConditions, null, null);
        
        // 构建完整的DSL查询
        Map<String, Object> dslQuery = buildDslQuery(boolQuery, size, null, null, includeFields, null);
        
        // 执行查询
        String dslQueryJson = convertToJson(dslQuery);
        return executeDslQuery(indexName + "/_search", "GET", dslQueryJson);
    }
} 