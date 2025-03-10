package com.example.kefu.controller;

import com.example.kefu.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public String chat(@RequestBody String question) {
        return deepSeekService.chat(question);
    }
} 