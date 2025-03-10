package com.example.kefu.controller;

import com.example.kefu.model.EmbeddingRequest;
import com.example.kefu.model.EmbeddingResponse;
import com.example.kefu.service.AliEmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 向量嵌入控制器
 */
@RestController
@RequestMapping("/api/v1/vector")
public class EmbeddingController {

    private final AliEmbeddingService aliEmbeddingService;

    @Autowired
    public EmbeddingController(AliEmbeddingService aliEmbeddingService) {
        this.aliEmbeddingService = aliEmbeddingService;
    }

    /**
     * 获取文本向量嵌入
     *
     * @param request 嵌入请求
     * @return 嵌入响应
     */
    @PostMapping("/embedding")
    public EmbeddingResponse getEmbedding(@RequestBody EmbeddingRequest request) {
        return aliEmbeddingService.getEmbedding(request);
    }
} 