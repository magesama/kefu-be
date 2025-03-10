package com.example.kefu.service;

import com.example.kefu.model.ChatMessage;
import com.example.kefu.model.ChatRequest;
import com.example.kefu.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeepSeekService {

    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

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
} 