package com.example.kefu.service.impl;

import com.example.kefu.model.log.ApiLogRecord;
import com.example.kefu.model.log.ErrorLogRecord;
import com.example.kefu.service.LogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.ValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志服务实现类
 */
@Slf4j
@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${elasticsearch.indices.log}")
    private String logIndex;
    
    @Value("${elasticsearch.indices.error}")
    private String errorLogIndex;
    
    @Override
    public void saveApiLog(ApiLogRecord apiLogRecord) {
        try {
            Map<String, Object> jsonMap = objectMapper.convertValue(apiLogRecord, Map.class);
            IndexRequest indexRequest = new IndexRequest(logIndex)
                    .source(jsonMap);
            elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("保存API日志失败", e);
        }
    }
    
    @Override
    public void saveErrorLog(ErrorLogRecord errorLogRecord) {
        try {
            Map<String, Object> jsonMap = objectMapper.convertValue(errorLogRecord, Map.class);
            IndexRequest indexRequest = new IndexRequest(errorLogIndex)
                    .source(jsonMap);
            elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("保存错误日志失败", e);
        }
    }
    
    @Override
    public long countRequestByUserIdAndDate(String userId, String date) {
        try {
            // 构建查询条件
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("userId", userId))
                    .must(QueryBuilders.rangeQuery("requestTime")
                            .gte(date + "T00:00:00.000Z")
                            .lte(date + "T23:59:59.999Z"));
            
            // 构建聚合查询
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .query(boolQueryBuilder)
                    .size(0) // 不需要返回文档，只需要聚合结果
                    .aggregation(AggregationBuilders.count("request_count").field("_id"));
            
            // 执行查询
            SearchRequest searchRequest = new SearchRequest(logIndex)
                    .source(searchSourceBuilder);
            SearchResponse searchResponse = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT);
            
            // 获取聚合结果
            ValueCount count = searchResponse.getAggregations().get("request_count");
            return count.getValue();
        } catch (IOException e) {
            log.error("统计用户请求数量失败", e);
            return 0;
        }
    }
} 