-- 文档表
CREATE TABLE IF NOT EXISTS `user_document` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `product_id` BIGINT(20) NOT NULL COMMENT '产品ID',
    `document_name` VARCHAR(255) NOT NULL COMMENT '文档名称',
    `document_type` VARCHAR(50) NOT NULL COMMENT '文档类型',
    `file_size` BIGINT(20) NOT NULL COMMENT '文件大小(字节)',
    `file_url` VARCHAR(500) NOT NULL COMMENT 'MinIO文件URL',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '文档描述',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文档表'; 