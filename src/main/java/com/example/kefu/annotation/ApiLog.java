package com.example.kefu.annotation;

import java.lang.annotation.*;

/**
 * API日志注解，用于标记需要记录日志的接口
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {
    
    /**
     * 接口描述
     */
    String value() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean recordRequestParams() default true;
    
    /**
     * 是否记录响应结果
     */
    boolean recordResponseBody() default true;
} 