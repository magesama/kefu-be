package com.example.kefu.model.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 用户文档请求模型
 */
@Data
public class UserDocumentRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 产品ID
     */
    @NotNull(message = "产品ID不能为空")
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
    @NotBlank(message = "文档名称不能为空")
    private String documentName;
    
    /**
     * 文档描述
     */
    private String description;
} 