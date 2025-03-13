-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
    `name` VARCHAR(100) NOT NULL COMMENT '知识库名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '知识库描述',
    `user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库表';

-- 知识库文档表
CREATE TABLE IF NOT EXISTS `knowledge_document` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `knowledge_base_id` BIGINT(20) NOT NULL COMMENT '所属知识库ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文档标题',
    `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型：pdf, docx, txt等',
    `file_size` BIGINT(20) NOT NULL COMMENT '文件大小(字节)',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-处理完成，3-处理失败',
    `process_message` VARCHAR(500) DEFAULT NULL COMMENT '处理消息，记录处理过程或错误信息',
    `chunk_count` INT(11) DEFAULT 0 COMMENT '文档分块数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_base_id` (`knowledge_base_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- 文档块表
CREATE TABLE IF NOT EXISTS `document_chunk` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文档块ID',
    `document_id` BIGINT(20) NOT NULL COMMENT '所属文档ID',
    `content` TEXT NOT NULL COMMENT '文本内容',
    `chunk_index` INT(11) NOT NULL COMMENT '块索引，表示在文档中的顺序',
    `vector_id` VARCHAR(100) DEFAULT NULL COMMENT '向量ID，对应ES中的文档ID',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待向量化，1-向量化中，2-向量化完成，3-向量化失败',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档块表';

-- 知识库问答表
CREATE TABLE IF NOT EXISTS `knowledge_qa` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '问答ID',
    `knowledge_base_id` BIGINT(20) NOT NULL COMMENT '所属知识库ID',
    `question` VARCHAR(500) NOT NULL COMMENT '问题',
    `answer` TEXT NOT NULL COMMENT '答案',
    `vector_id` VARCHAR(100) DEFAULT NULL COMMENT '向量ID，对应ES中的文档ID',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待向量化，1-向量化中，2-向量化完成，3-向量化失败',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_base_id` (`knowledge_base_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库问答表'; 