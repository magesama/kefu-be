package com.example.kefu.model.log;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * API日志记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogRecord {
    
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
     * 响应体
     */
    private String responseBody;
    
    /**
     * 状态（success/fail）
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行时间（毫秒）
     */
    private Long executionTime;
    
    /**
     * 类名
     */
    private String className;
    
    /**
     * 方法名
     */
    private String methodName;
} 