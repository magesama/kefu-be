package com.example.kefu.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 向量聊天请求类，扩展了ChatRequest，添加了向量字段选择功能
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VectorChatRequest extends ChatRequest {
    /**
     * 向量字段名称，可选值：question_vector（问题向量）或answer_vector（答案向量）
     */
    private String vectorField;
} 