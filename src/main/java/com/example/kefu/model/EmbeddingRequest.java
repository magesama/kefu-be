package com.example.kefu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量嵌入请求模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {
    /**
     * 输入文本
     */
    private String input;
    
    /**
     * 模型名称，默认为text-embedding-v3
     */
    private String model = "text-embedding-v3";
    
    /**
     * 向量维度，默认为512
     */
    private Integer dimensions = 512;
    
    /**
     * 编码格式，默认为float
     */
    private String encodingFormat = "float";
} 