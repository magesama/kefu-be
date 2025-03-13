package com.example.kefu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档实体类
 */
@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    
    /**
     * 文档ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 所属知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文件类型：pdf, docx, txt等
     */
    private String fileType;
    
    /**
     * 文件大小(字节)
     */
    private Long fileSize;
    
    /**
     * 文件存储路径
     */
    private String filePath;
    
    /**
     * 状态：0-待处理，1-处理中，2-处理完成，3-处理失败
     */
    private Integer status;
    
    /**
     * 处理消息，记录处理过程或错误信息
     */
    private String processMessage;
    
    /**
     * 文档分块数量
     */
    private Integer chunkCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 