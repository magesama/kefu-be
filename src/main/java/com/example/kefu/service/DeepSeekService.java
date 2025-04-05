package com.example.kefu.service;

import com.example.kefu.model.ChatMessage;
import com.example.kefu.model.ChatRequest;
import com.example.kefu.model.ChatResponse;
import com.example.kefu.model.response.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

@Service
public class DeepSeekService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.api.url}")
    private String apiUrl;

    @Value("${deepseek.api.model}")
    private String model;

    public String chat(String question) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", question));

        ChatRequest request = new ChatRequest();
        request.setModel(model);
        request.setMessages(messages);
        request.setTemperature(0.7);
        request.setMax_tokens(2000);

        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

        ChatResponse response = restTemplate.postForObject(apiUrl, entity, ChatResponse.class);

        if (response != null && response.getChoices() != null && response.getChoices().length > 0) {
            return response.getChoices()[0].getMessage().getContent();
        } else {
            return "无法获取回复";
        }
    }


    
    /**
     * 判断是否为产品相关问题
     */
    private boolean isProductRelatedQuestion(String intent) {
        // TODO: 根据意图识别结果判断是否为产品相关问题
        return intent.contains("产品") || intent.contains("商品");
    }
    
    /**
     * 生成文本向量
     */
    private float[] generateVector(String text) {
        // TODO: 实现文本转向量
        return new float[512]; // 临时返回空向量
    }
    


} 