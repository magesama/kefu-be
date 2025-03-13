package com.example.kefu.service;

import com.example.kefu.model.log.ApiLogRecord;
import com.example.kefu.model.log.ErrorLogRecord;

/**
 * 日志服务接口
 */
public interface LogService {
    
    /**
     * 保存API日志
     * @param apiLogRecord API日志记录
     */
    void saveApiLog(ApiLogRecord apiLogRecord);
    
    /**
     * 保存错误日志
     * @param errorLogRecord 错误日志记录
     */
    void saveErrorLog(ErrorLogRecord errorLogRecord);
    
    /**
     * 根据用户ID统计指定日期的请求数量
     * @param userId 用户ID
     * @param date 日期，格式：yyyy-MM-dd
     * @return 请求数量
     */
    long countRequestByUserIdAndDate(String userId, String date);
} 