package com.example.kefu.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体类
 */
@Data
public class Product {
    /**
     * 产品ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 产品名称
     */
    private String name;
    
    /**
     * 产品描述
     */
    private String description;
    
    /**
     * 产品价格
     */
    private BigDecimal price;
    
    /**
     * 产品库存
     */
    private Integer stock;
    
    /**
     * 产品分类
     */
    private String category;
    
    /**
     * 产品状态：0-下架，1-上架
     */
    private Integer status;
    
    /**
     * 删除标识：0-未删除，1-已删除
     */
    private Integer isDeleted;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 