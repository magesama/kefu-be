package com.example.kefu.aspect;

import com.example.kefu.annotation.ApiLog;
import com.example.kefu.util.LogUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * API日志切面
 */
@Slf4j
@Aspect
@Component
public class ApiLogAspect {

    @Autowired
    private LogUtil logUtil;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 定义切点
     */
    @Pointcut("@annotation(com.example.kefu.annotation.ApiLog)")
    public void apiLogPointcut() {
    }
    
    /**
     * 环绕通知
     */
    @Around("apiLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String apiPath = request.getRequestURI();
        
        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = method.getName();
        
        // 获取注解信息
        ApiLog apiLog = method.getAnnotation(ApiLog.class);
        
        // 获取请求参数
        String requestBody = "";
        if (apiLog.recordRequestParams()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    requestBody = objectMapper.writeValueAsString(args);
                } catch (Exception e) {
                    log.error("序列化请求参数失败", e);
                }
            }
        }
        
        // 获取用户ID，这里假设从请求头中获取，实际项目中可能需要从Token或Session中获取
        String userId = request.getHeader("X-User-Id");
        
        Object result = null;
        String responseBody = "";
        String status = "success";
        String errorMessage = "";
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            
            // 获取响应结果
            if (apiLog.recordResponseBody() && result != null) {
                try {
                    responseBody = objectMapper.writeValueAsString(result);
                } catch (Exception e) {
                    log.error("序列化响应结果失败", e);
                }
            }
            
            return result;
        } catch (Throwable e) {
            status = "fail";
            errorMessage = e.getMessage();
            
            // 记录错误日志
            logUtil.recordErrorLog(userId, apiPath, requestBody, e, className, methodName, "ERROR");
            
            throw e;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录API日志
            logUtil.recordApiLog(userId, apiPath, requestBody, responseBody, status, errorMessage, executionTime, className, methodName);
        }
    }
} 