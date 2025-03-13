package com.example.kefu.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字段信息响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldInfo {
    
    /**
     * 字段名称
     */
    private String name;
    
    /**
     * 字段类型
     */
    private String type;
} 