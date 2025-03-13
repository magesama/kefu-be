package com.example.kefu.model.response;

import lombok.Data;

/**
 * 搜索结果响应类
 */
@Data
public class SearchResult {
    /**
     * 问题
     */
    private String question;

    /**
     * 答案
     */
    private String answer;

    /**
     * 相似度分数
     */
    private float score;

    /**
     * 向量
     */
    private float[] vector;
} 