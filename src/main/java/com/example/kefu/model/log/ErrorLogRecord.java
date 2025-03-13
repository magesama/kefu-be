package com.example.kefu.model.log;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 错误日志记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogRecord {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * API路径
     */
    private String apiPath;
    
    /**
     * 请求时间
     */
    private Date requestTime;
    
    /**
     * 请求方法（GET, POST等）
     */
    private String method;
    
    /**
     * 请求IP
     */
    private String ip;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 请求体
     */
    private String requestBody;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 错误堆栈
     */
    private String errorStack;
    
    /**
     * 类名
     */
    private String className;
    
    /**
     * 方法名
     */
    private String methodName;
    
    /**
     * 错误类型
     */
    private String errorType;
    
    /**
     * 错误级别（ERROR, WARN等）
     */
    private String errorLevel;
} 