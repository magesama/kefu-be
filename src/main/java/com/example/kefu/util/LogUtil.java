package com.example.kefu.util;

import com.example.kefu.model.log.ApiLogRecord;
import com.example.kefu.model.log.ErrorLogRecord;
import com.example.kefu.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * 日志工具类
 */
@Slf4j
@Component
public class LogUtil {

    @Autowired
    private LogService logService;
    
    /**
     * 记录API日志
     * @param userId 用户ID
     * @param apiPath API路径
     * @param requestBody 请求体
     * @param responseBody 响应体
     * @param status 状态
     * @param errorMessage 错误信息
     * @param executionTime 执行时间
     * @param className 类名
     * @param methodName 方法名
     */
    public void recordApiLog(String userId, String apiPath, String requestBody, String responseBody,
                            String status, String errorMessage, Long executionTime,
                            String className, String methodName) {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest");
                return;
            }
            
            ApiLogRecord apiLogRecord = ApiLogRecord.builder()
                    .userId(userId)
                    .apiPath(apiPath)
                    .requestTime(new Date())
                    .method(request.getMethod())
                    .ip(getIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .requestBody(requestBody)
                    .responseBody(responseBody)
                    .status(status)
                    .errorMessage(errorMessage)
                    .executionTime(executionTime)
                    .className(className)
                    .methodName(methodName)
                    .build();
            
            logService.saveApiLog(apiLogRecord);
        } catch (Exception e) {
            log.error("记录API日志失败", e);
        }
    }
    
    /**
     * 记录错误日志
     * @param userId 用户ID
     * @param apiPath API路径
     * @param requestBody 请求体
     * @param e 异常
     * @param className 类名
     * @param methodName 方法名
     * @param errorLevel 错误级别
     */
    public void recordErrorLog(String userId, String apiPath, String requestBody, Throwable e,
                              String className, String methodName, String errorLevel) {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest");
                return;
            }
            
            ErrorLogRecord errorLogRecord = ErrorLogRecord.builder()
                    .userId(userId)
                    .apiPath(apiPath)
                    .requestTime(new Date())
                    .method(request.getMethod())
                    .ip(getIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .requestBody(requestBody)
                    .errorMessage(e.getMessage())
                    .errorStack(getStackTrace(e))
                    .className(className)
                    .methodName(methodName)
                    .errorType(e.getClass().getName())
                    .errorLevel(errorLevel)
                    .build();
            
            logService.saveErrorLog(errorLogRecord);
        } catch (Exception ex) {
            log.error("记录错误日志失败", ex);
        }
    }
    
    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取异常堆栈信息
     */
    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }
} 