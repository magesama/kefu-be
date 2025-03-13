package com.example.kefu.config;

import com.example.kefu.model.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理参数校验异常
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验异常: {}", errorMessage);
        return ApiResponse.error(400, errorMessage);
    }
    
    /**
     * 处理参数绑定异常
     * @param e 参数绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常: {}", errorMessage);
        return ApiResponse.error(400, errorMessage);
    }
    
    /**
     * 处理约束违反异常
     * @param e 约束违反异常
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.error("约束违反异常: {}", errorMessage);
        return ApiResponse.error(400, errorMessage);
    }
    
    /**
     * 处理运行时异常
     * @param e 运行时异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return ApiResponse.error(500, e.getMessage());
    }
    
    /**
     * 处理所有其他异常
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return ApiResponse.error(500, "系统异常，请联系管理员");
    }
} 