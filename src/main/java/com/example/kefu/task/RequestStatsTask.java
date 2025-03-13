package com.example.kefu.task;

import com.example.kefu.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 请求统计定时任务
 */
@Slf4j
@Component
public class RequestStatsTask {

    @Autowired
    private LogService logService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 每天凌晨1点执行统计任务
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void statisticsRequestCount() {
        try {
            log.info("开始执行请求统计任务");
            
            // 获取昨天的日期
            String yesterday = DATE_FORMAT.format(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
            
            // 查询所有用户
            List<Map<String, Object>> users = jdbcTemplate.queryForList("SELECT id FROM t_user WHERE status = 1");
            
            for (Map<String, Object> user : users) {
                Long userId = (Long) user.get("id");
                
                // 统计用户昨天的请求数量
                long requestCount = logService.countRequestByUserIdAndDate(userId.toString(), yesterday);
                
                // 更新或插入统计数据
                int updated = jdbcTemplate.update(
                        "UPDATE t_request_stats SET request_count = ?, update_time = NOW() " +
                                "WHERE user_id = ? AND request_date = ?",
                        requestCount, userId, yesterday);
                
                if (updated == 0) {
                    jdbcTemplate.update(
                            "INSERT INTO t_request_stats (user_id, request_date, request_count, create_time, update_time) " +
                                    "VALUES (?, ?, ?, NOW(), NOW())",
                            userId, yesterday, requestCount);
                }
                
                log.info("用户 {} 在 {} 的请求数量为: {}", userId, yesterday, requestCount);
            }
            
            log.info("请求统计任务执行完成");
        } catch (Exception e) {
            log.error("请求统计任务执行失败", e);
        }
    }
} 