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
     * 回答用户问题
     * 
     * @param question 用户问题
     * @param userId 用户ID，可选
     * @return 回答内容
     */
    public String answer(String question, Integer userId) {
        // 1. 意图识别
        String intentPrompt = ""; // TODO: 补充意图识别提示词
        String intent = chat(intentPrompt + question);
        
        // 如果不是产品相关问题，直接返回闲聊回答
        if (!isProductRelatedQuestion(intent)) {
            return chat(question);
        }
        
        // 2. RAG检索流程
        // 2.1 向量检索
        List<SearchResult> vectorResults = elasticsearchService.searchByVector(
            generateVector(question),  // TODO: 实现文本转向量方法
            5,  // top k
            1.8f  // 相似度阈值
        );
        
        // 2.2 全文检索
        List<SearchResult> textResults = elasticsearchService.searchByText(
            question,
            5,  // top k
            1.8f  // 相似度分数阈值
        );
        
        // 2.3 合并结果
        Set<SearchResult> uniqueResults = new LinkedHashSet<>();
        uniqueResults.addAll(vectorResults);
        uniqueResults.addAll(textResults);
        
        // 2.4 构建上下文
        String context = buildContext(uniqueResults);
        
        // 2.5 生成最终答案
        String ragPrompt = ""; // TODO: 补充RAG提示词
        return chat(ragPrompt + "\n上下文：" + context + "\n问题：" + question);
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
    
    /**
     * 构建上下文信息
     */
    private String buildContext(Collection<SearchResult> results) {
        StringBuilder context = new StringBuilder();
        for (SearchResult result : results) {
            context.append("问：").append(result.getQuestion())
                  .append("\n答：").append(result.getAnswer())
                  .append("\n\n");
        }
        return context.toString();
    }

} 