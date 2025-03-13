package com.example.kefu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档块实体类
 */
@Data
@TableName("document_chunk")
public class DocumentChunk {
    
    /**
     * 文档块ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属文档ID
     */
    private Long documentId;
    
    /**
     * 文本内容
     */
    private String content;
    
    /**
     * 块索引，表示在文档中的顺序
     */
    private Integer chunkIndex;
    
    /**
     * 向量ID，对应ES中的文档ID
     */
    private String vectorId;
    
    /**
     * 状态：0-待向量化，1-向量化中，2-向量化完成，3-向量化失败
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 