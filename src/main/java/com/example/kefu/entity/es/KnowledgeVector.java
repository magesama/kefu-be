package com.example.kefu.entity.es;

import lombok.Data;
import org.elasticsearch.common.Nullable;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * 知识库向量ES实体类
 */
@Data
public class KnowledgeVector {

    /**
     * ES文档ID
     */
    @Nullable
    @JsonProperty("_id")
    private String id;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 所属知识库ID
     */
    private Long knowledgeBaseId;

    /**
     * 所属文档ID（如果是文档块）
     */
    private Long documentId;

    /**
     * 所属问答ID（如果是问答）
     */
    private Long qaId;

    /**
     * 类型：1-文档块，2-问答
     */
    private Integer type;

    /**
     * 向量数据
     */
    private float[] vector;

    /**
     * 创建时间
     */
    private Date createTime;
} 