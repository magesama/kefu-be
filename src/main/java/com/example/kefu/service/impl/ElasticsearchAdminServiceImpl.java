package com.example.kefu.service.impl;

import com.example.kefu.model.request.AdvancedSearchRequest;

import com.example.kefu.model.response.*;
import com.example.kefu.service.ElasticsearchAdminService;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;

import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch管理服务实现类
 */
@Service
public class ElasticsearchAdminServiceImpl implements ElasticsearchAdminService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<IndexInfo> getAllIndices() {
        try {
            // 获取所有索引
            org.elasticsearch.client.indices.GetIndexRequest request = new org.elasticsearch.client.indices.GetIndexRequest("*");
            org.elasticsearch.client.indices.GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            
            List<IndexInfo> indices = new ArrayList<>();
            for (String indexName : response.getIndices()) {
                // 获取索引信息
                IndexInfo indexInfo = getBasicIndexInfo(indexName, response.getSettings().get(indexName));
                indices.add(indexInfo);
            }
            
            return indices;
        } catch (IOException e) {
            throw new RuntimeException("获取索引列表失败", e);
        }
    }

    @Override
    public IndexInfo getIndexInfo(String indexName) {
        try {
            // 获取索引信息
            org.elasticsearch.client.indices.GetIndexRequest request = new org.elasticsearch.client.indices.GetIndexRequest(indexName);
            org.elasticsearch.client.indices.GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            
            // 获取索引映射
            GetMappingsRequest mappingsRequest = new GetMappingsRequest().indices(indexName);
            GetMappingsResponse mappingsResponse = client.indices().getMapping(mappingsRequest, RequestOptions.DEFAULT);
            MappingMetadata mappingMetadata = mappingsResponse.mappings().get(indexName);
            
            // 构建索引详情
            IndexInfo indexInfo = getBasicIndexInfo(indexName, response.getSettings().get(indexName));
            indexInfo.setMappings(mappingMetadata.sourceAsMap());
            
            // 转换Settings为Map
            Map<String, Object> settingsMap = new HashMap<>();
            Settings settings = response.getSettings().get(indexName);
            settings.keySet().forEach(key -> settingsMap.put(key, settings.get(key)));
            
            indexInfo.setSettings(settingsMap);
            
            return indexInfo;
        } catch (IOException e) {
            throw new RuntimeException("获取索引详情失败: " + indexName, e);
        }
    }

    @Override
    public SearchResult searchIndex(String indexName, com.example.kefu.model.request.SearchRequest request) {
        try {
            // 创建搜索请求
            org.elasticsearch.action.search.SearchRequest searchRequest = new org.elasticsearch.action.search.SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 设置查询条件
            if (request.getQuery() != null && !request.getQuery().isEmpty()) {
                // 这里简单处理，实际应该根据查询条件构建复杂的查询
                sourceBuilder.query(QueryBuilders.matchAllQuery()); // 简化处理，实际应该解析query对象
            } else {
                sourceBuilder.query(QueryBuilders.matchAllQuery());
            }

            // 设置分页
            sourceBuilder.from(request.getFrom());
            sourceBuilder.size(request.getSize());

            // 设置排序
            if (request.getSort() != null && !request.getSort().isEmpty()) {
                for (Map<String, Map<String, String>> sortItem : request.getSort()) {
                    for (Map.Entry<String, Map<String, String>> entry : sortItem.entrySet()) {
                        String field = entry.getKey();
                        String order = entry.getValue().get("order");
                        sourceBuilder.sort(field, "desc".equalsIgnoreCase(order) ? SortOrder.DESC : SortOrder.ASC);
                    }
                }
            }

            // 设置高亮
            if (request.getHighlight() != null && !request.getHighlight().isEmpty()) {
                // 简化处理，实际应该解析highlight对象
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                // 添加默认高亮字段
                highlightBuilder.field("*");
                sourceBuilder.highlighter(highlightBuilder);
            }

            searchRequest.source(sourceBuilder);

            // 执行搜索
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            // 处理搜索结果
            return processSearchResponse(searchResponse);
        } catch (IOException e) {
            throw new RuntimeException("搜索索引失败: " + indexName, e);
        }
    }

    @Override
    public SearchResult advancedSearch(String indexName, AdvancedSearchRequest request) {
        try {
            // 创建搜索请求
            org.elasticsearch.action.search.SearchRequest searchRequest = new org.elasticsearch.action.search.SearchRequest(indexName);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            // 构建查询条件
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            
            if (request.getConditions() != null && !request.getConditions().isEmpty()) {
                for (AdvancedSearchRequest.SearchCondition condition : request.getConditions()) {
                    QueryBuilder queryBuilder = buildQueryFromCondition(condition);
                    
                    if ("and".equalsIgnoreCase(request.getLogicalOperator())) {
                        boolQuery.must(queryBuilder);
                    } else {
                        boolQuery.should(queryBuilder);
                    }
                }
                
                if ("or".equalsIgnoreCase(request.getLogicalOperator()) && !request.getConditions().isEmpty()) {
                    boolQuery.minimumShouldMatch(1);
                }
            } else {
                boolQuery.must(QueryBuilders.matchAllQuery());
            }
            
            sourceBuilder.query(boolQuery);
            
            // 设置分页
            sourceBuilder.from(request.getFrom());
            sourceBuilder.size(request.getSize());
            
            // 设置排序
            if (request.getSort() != null && !request.getSort().isEmpty()) {
                for (AdvancedSearchRequest.SortField sortField : request.getSort()) {
                    sourceBuilder.sort(sortField.getField(), 
                            "desc".equalsIgnoreCase(sortField.getOrder()) ? SortOrder.DESC : SortOrder.ASC);
                }
            }
            
            searchRequest.source(sourceBuilder);
            
            // 执行搜索
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            
            // 处理搜索结果
            return processSearchResponse(searchResponse);
        } catch (IOException e) {
            throw new RuntimeException("高级搜索失败: " + indexName, e);
        }
    }

    @Override
    public List<FieldInfo> getIndexFields(String indexName) {
        try {
            // 获取索引映射
            GetMappingsRequest request = new GetMappingsRequest().indices(indexName);
            GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);
            MappingMetadata mappingMetadata = response.mappings().get(indexName);
            
            // 解析字段信息
            Map<String, Object> mappings = mappingMetadata.sourceAsMap();
            Map<String, Object> properties = (Map<String, Object>) mappings.get("properties");
            
            List<FieldInfo> fields = new ArrayList<>();
            if (properties != null) {
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String fieldName = entry.getKey();
                    Map<String, Object> fieldProperties = (Map<String, Object>) entry.getValue();
                    String fieldType = (String) fieldProperties.get("type");
                    
                    fields.add(FieldInfo.builder()
                            .name(fieldName)
                            .type(fieldType)
                            .build());
                }
            }
            
            return fields;
        } catch (IOException e) {
            throw new RuntimeException("获取索引字段信息失败: " + indexName, e);
        }
    }

    /**
     * 获取基本索引信息
     */
    private IndexInfo getBasicIndexInfo(String indexName, Settings settings) {
        try {
            // 获取索引统计信息
            CountRequest countRequest = new CountRequest(indexName);
            CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
            long docsCount = countResponse.getCount();
            
            // 简化实现，不获取健康状态
            return IndexInfo.builder()
                    .name(indexName)
                    .docsCount(docsCount)
                    .docsDeleted(0L) // 需要额外查询
                    .storeSizeBytes(0L) // 需要额外查询
                    .health("unknown") // 不获取健康状态
                    .status("open") // 假设都是open状态
                    .creationDate(new Date(settings.getAsLong("index.creation_date", 0L)))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("获取索引基本信息失败: " + indexName, e);
        }
    }

    /**
     * 处理搜索响应
     */
    private SearchResult processSearchResponse(SearchResponse response) {
        // 先转换为ElasticsearchResult
        ElasticsearchResult esResult = processElasticsearchResponse(response);
        
        // 再转换为SearchResult
        SearchResult result = new SearchResult();
        if (!esResult.getHits().isEmpty()) {
            ElasticsearchResult.Hit hit = esResult.getHits().get(0);
            result.setQuestion((String) hit.getSource().get("question"));
            result.setAnswer((String) hit.getSource().get("answer"));
            result.setScore(hit.getScore());
            // TODO: 如果需要vector字段，从source中获取
        }
        return result;
    }

    /**
     * 处理ES搜索响应
     */
    private ElasticsearchResult processElasticsearchResponse(SearchResponse response) {
        List<ElasticsearchResult.Hit> hits = new ArrayList<>();
        
        for (SearchHit hit : response.getHits().getHits()) {
            Map<String, List<String>> highlightFields = new HashMap<>();
            if (hit.getHighlightFields() != null && !hit.getHighlightFields().isEmpty()) {
                hit.getHighlightFields().forEach((key, value) -> {
                    if (value.fragments() != null && value.fragments().length > 0) {
                        highlightFields.put(key, Arrays.asList(value.fragments()[0].string()));
                    }
                });
            }
            
            hits.add(ElasticsearchResult.Hit.builder()
                    .id(hit.getId())
                    .source(hit.getSourceAsMap())
                    .highlight(highlightFields)
                    .score(hit.getScore())
                    .build());
        }
        
        return ElasticsearchResult.builder()
                .total(response.getHits().getTotalHits().value)
                .hits(hits)
                .build();
    }

    /**
     * 根据条件构建查询
     */
    private QueryBuilder buildQueryFromCondition(AdvancedSearchRequest.SearchCondition condition) {
        String field = condition.getField();
        Object value = condition.getValue();
        String operator = condition.getOperator();
        
        switch (operator) {
            case "eq":
                return QueryBuilders.termQuery(field, value);
            case "ne":
                return QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(field, value));
            case "gt":
                return QueryBuilders.rangeQuery(field).gt(value);
            case "lt":
                return QueryBuilders.rangeQuery(field).lt(value);
            case "gte":
                return QueryBuilders.rangeQuery(field).gte(value);
            case "lte":
                return QueryBuilders.rangeQuery(field).lte(value);
            case "like":
                return QueryBuilders.wildcardQuery(field, "*" + value + "*");
            default:
                return QueryBuilders.matchQuery(field, value);
        }
    }
} 