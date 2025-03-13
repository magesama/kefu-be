package com.example.kefu.model.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户文档响应模型
 */
@Data
public class UserDocumentResponse {
    
    /**
     * 文档ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 产品ID
     */
    private Long productId;
    
    /**
     * 产品名称
     */
    private String productName;
    
    /**
     * 店铺ID
     */
    private Long shopId;
    
    /**
     * 店铺名称
     */
    private String shopName;
    
    /**
     * 文档名称
     */
    private String documentName;
    
    /**
     * 文档类型
     */
    private String documentType;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文档描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 