package com.example.kefu.model.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 搜索请求模型
 */
@Data
public class SearchRequest {
    /**
     * 查询条件
     */
    private Map<String, Object> query;
    
    /**
     * 分页起始位置
     */
    private int from = 0;
    
    /**
     * 分页大小
     */
    private int size = 10;
    
    /**
     * 排序条件
     * 格式: [{"field1": {"order": "desc"}}, {"field2": {"order": "asc"}}]
     */
    private List<Map<String, Map<String, String>>> sort;
    
    /**
     * 高亮配置
     */
    private Map<String, Object> highlight;
} 