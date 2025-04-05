package com.example.kefu.model.request;

import lombok.Data;

/**
 * 聊天请求类
 */
@Data
public class ChatRequest {
    /**
     * 用户问题
     */
    private String question;

    private String productName;

    private String shopName;


    private Integer userId;
    
    /**
     * 聊天窗口ID，用于标识多轮对话的上下文
     */
    private String tableId;
} 