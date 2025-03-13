package com.example.kefu.model.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * QA数据上传请求类
 */
@Data
public class QADataRequest {
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * QA列表，每个元素是一个Map，包含question和answer字段
     */
    @NotEmpty(message = "QA列表不能为空")
    private List<Map<String, String>> qaList;
} 