package com.example.kefu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库问答实体类
 */
@Data
@TableName("knowledge_qa")
public class KnowledgeQA {
    
    /**
     * 问答ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 问题
     */
    private String question;
    
    /**
     * 答案
     */
    private String answer;
    
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