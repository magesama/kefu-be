package com.example.kefu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.kefu.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库文档Mapper接口
 */
@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
} 