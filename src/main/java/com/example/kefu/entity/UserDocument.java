package com.example.kefu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户文档实体类
 */
@Data
@TableName("user_document")
public class UserDocument {
    
    /**
     * 文档ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * MinIO文件URL
     */
    private String fileUrl;
    
    /**
     * 文档描述
     */
    private String description;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableLogic
    private Boolean isDeleted;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 