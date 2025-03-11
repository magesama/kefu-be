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
     * @param mappingJson 索引映射JSON结构
     * @return 是否成功创建（如果已存在则返回false）
     * @throws IOException 如果创建过程中发生IO异常
     */
    public boolean createCustomIndex(String indexName, String mappingJson) throws IOException {
        // 检查索引是否已存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);

        if (exists) {
            return false;
        }

        // 创建索引请求
        CreateIndexRequest request = new CreateIndexRequest(indexName);

        // 设置映射
        request.mapping(mappingJson, XContentType.JSON);

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
        
        // 构建向量查询
        Map<String, Object> knnQuery = new HashMap<>();
        knnQuery.put("query_vector", vector);
        knnQuery.put("k", size);
        knnQuery.put("num_candidates", size * 2);
        
        searchSourceBuilder.query(
            QueryBuilders.scriptScoreQuery(
                QueryBuilders.matchAllQuery(),
                new Script(
                    ScriptType.INLINE,
                    "painless",
                    "cosineSimilarity(params.query_vector, doc['vector']) + 1.0",
                    Collections.singletonMap("query_vector", vector)
                )
            )
        );
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
} 