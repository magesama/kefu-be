package com.example.kefu.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 向量嵌入响应模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingResponse {
    private String object;
    private List<EmbeddingData> data;
    private String model;
    private Usage usage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmbeddingData {
        private List<Float> embedding;
        private Integer index;
        private String object;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        private Integer promptTokens;
        private Integer totalTokens;
    }
} 