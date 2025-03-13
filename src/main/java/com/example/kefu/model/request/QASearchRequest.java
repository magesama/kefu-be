package com.example.kefu.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 问答搜索请求类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QASearchRequest {
    /**
     * 搜索文本
     */
    private String text;
    
    /**
     * 向量数据
     */
    private float[] vector;
    
    /**
     * 相似度阈值
     */
    @Builder.Default
    private float threshold = 1.8f;
    
    /**
     * 返回结果数量
     */
    @Builder.Default
    private int topK = 5;
    
    /**
     * 搜索类型：vector-向量搜索，text-全文搜索，both-两者都用
     */
    @Builder.Default
    private String searchType = "both";
} 