package com.example.kefu.controller;

import com.example.kefu.annotation.ApiLog;
import com.example.kefu.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 示例控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @Autowired
    private LogUtil logUtil;

    /**
     * 测试API日志注解
     */
    @ApiLog("测试API日志")
    @GetMapping("/test")
    public Map<String, Object> test(@RequestParam(required = false) String param) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Hello, " + (param != null ? param : "World"));
        return result;
    }

    /**
     * 测试异常日志
     */
    @ApiLog("测试异常日志")
    @GetMapping("/error")
    public Map<String, Object> error() {
        throw new RuntimeException("测试异常");
    }

    /**
     * 测试手动记录错误日志
     */
    @GetMapping("/manual-error")
    public Map<String, Object> manualError(@RequestParam(required = false) String userId) {
        try {
            // 模拟业务逻辑
            if (userId == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return result;
        } catch (Exception e) {
            // 手动记录错误日志
            logUtil.recordErrorLog(
                    userId != null ? userId : "anonymous",
                    "/api/demo/manual-error",
                    "userId=" + userId,
                    e,
                    this.getClass().getName(),
                    "manualError",
                    "WARN"
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }
} 