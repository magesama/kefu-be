package com.example.kefu.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

/**
 * 索引信息响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexInfo {
    
    /**
     * 索引名称
     */
    private String name;
    
    /**
     * 文档数量
     */
    private Long docsCount;
    
    /**
     * 已删除文档数量
     */
    private Long docsDeleted;
    
    /**
     * 存储大小（字节）
     */
    private Long storeSizeBytes;
    
    /**
     * 健康状态：green, yellow, red
     */
    private String health;
    
    /**
     * 索引状态：open, close
     */
    private String status;
    
    /**
     * 创建时间
     */
    private Date creationDate;
    
    /**
     * 索引映射
     */
    private Map<String, Object> mappings;
    
    /**
     * 索引设置
     */
    private Map<String, Object> settings;
} 