package com.example.kefu.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 搜索结果响应类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 命中记录列表
     */
    private List<Hit> hits;
    
    /**
     * 搜索命中记录
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hit {
        /**
         * 文档ID
         */
        private String id;
        
        /**
         * 文档源数据
         */
        private Map<String, Object> source;
        
        /**
         * 高亮字段
         */
        private Map<String, List<String>> highlight;
        
        /**
         * 相似度分数
         */
        private float score;
    }
} 