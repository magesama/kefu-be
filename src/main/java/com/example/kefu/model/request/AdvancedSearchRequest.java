package com.example.kefu.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 高级搜索请求模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvancedSearchRequest {
    /**
     * 搜索条件列表
     */
    private List<SearchCondition> conditions;
    
    /**
     * 逻辑操作符：and 或 or
     */
    private String logicalOperator = "and";
    
    /**
     * 分页起始位置
     */
    private int from = 0;
    
    /**
     * 分页大小
     */
    private int size = 10;
    
    /**
     * 排序字段列表
     */
    private List<SortField> sort;
    
    /**
     * 搜索条件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchCondition {
        /**
         * 字段名
         */
        private String field;
        
        /**
         * 字段值
         */
        private Object value;
        
        /**
         * 操作符
         * eq: 等于
         * ne: 不等于
         * gt: 大于
         * lt: 小于
         * gte: 大于等于
         * lte: 小于等于
         * like: 模糊匹配
         */
        private String operator;
    }
    
    /**
     * 排序字段
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SortField {
        /**
         * 字段名
         */
        private String field;
        
        /**
         * 排序方向：asc 或 desc
         */
        private String order;
    }
} 