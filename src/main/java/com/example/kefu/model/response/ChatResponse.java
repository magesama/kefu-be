package com.example.kefu.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    
    /**
     * 回答内容
     */
    private String answer;
    
    /**
     * 创建一个包含回答内容的响应对象
     * 
     * @param answer 回答内容
     * @return 聊天响应对象
     */
    public static ChatResponse of(String answer) {
        return new ChatResponse(answer);
    }
} 