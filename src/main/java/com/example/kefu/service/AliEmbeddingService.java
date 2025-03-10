package com.example.kefu.service;

import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云向量模型服务
 */
@Service
public class AliEmbeddingService {

    private final String apiKey;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/embeddings";

    public AliEmbeddingService(@Value("${aliyun.dashscope.api-key:sk-f80333dbad6a4a78a79517fd0749cae1}") String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    /**
     * 获取文本向量嵌入
     *
     * @param request 嵌入请求
     * @return 嵌入响应
     */
    public EmbeddingResponse getEmbedding(EmbeddingRequest request) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", request.getModel());
            requestBody.put("input", request.getInput());
            requestBody.put("dimensions", request.getDimensions());
            requestBody.put("encoding_format", request.getEncodingFormat());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<EmbeddingResponse> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    httpEntity,
                    EmbeddingResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new RuntimeException("获取向量嵌入失败，状态码: " + response.getStatusCodeValue());
            }
        } catch (Exception e) {
            throw new RuntimeException("获取向量嵌入时发生错误: " + e.getMessage(), e);
        }
    }
} 