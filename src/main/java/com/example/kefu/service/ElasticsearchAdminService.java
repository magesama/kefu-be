package com.example.kefu.service;

import com.example.kefu.model.request.AdvancedSearchRequest;
import com.example.kefu.model.request.SearchRequest;
import com.example.kefu.model.response.*;

import java.util.List;

/**
 * Elasticsearch管理服务接口
 * 提供索引查询、数据展示和搜索筛选等功能
 */
public interface ElasticsearchAdminService {

    /**
     * 获取所有索引
     *
     * @return 索引列表
     */
    List<IndexInfo> getAllIndices();

    /**
     * 获取索引详情
     *
     * @param indexName 索引名称
     * @return 索引详情
     */
    IndexInfo getIndexInfo(String indexName);

    /**
     * 查询索引数据
     *
     * @param indexName 索引名称
     * @param request 搜索请求
     * @return 搜索结果
     */
    SearchResult searchIndex(String indexName, SearchRequest request);

    /**
     * 高级搜索
     *
     * @param indexName 索引名称
     * @param request 高级搜索请求
     * @return 搜索结果
     */
    SearchResult advancedSearch(String indexName, AdvancedSearchRequest request);

    /**
     * 获取索引字段信息
     *
     * @param indexName 索引名称
     * @return 字段信息列表
     */
    List<FieldInfo> getIndexFields(String indexName);
} 