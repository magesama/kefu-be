-- 创建kefu数据库
CREATE DATABASE IF NOT EXISTS `kefu` DEFAULT CHARACTER SET utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 使用kefu数据库
USE `kefu`;

-- 创建用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    `role` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建产品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '产品名称',
    `description` TEXT DEFAULT NULL COMMENT '产品描述',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '产品价格',
    `stock` INT(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '产品分类',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '产品状态：0-下架，1-上架',
    `is_deleted` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- 创建用户文档表
DROP TABLE IF EXISTS `user_document`;
CREATE TABLE `user_document` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '文档ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `product_id` BIGINT(20) DEFAULT NULL COMMENT '产品ID',
    `product_name` VARCHAR(100) DEFAULT NULL COMMENT '产品名称',
    `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
    `shop_name` VARCHAR(100) DEFAULT NULL COMMENT '店铺名称',
    `document_name` VARCHAR(255) NOT NULL COMMENT '文档名称',
    `document_type` VARCHAR(50) DEFAULT NULL COMMENT '文档类型',
    `file_size` BIGINT(20) NOT NULL COMMENT '文件大小(字节)',
    `file_url` VARCHAR(500) NOT NULL COMMENT 'MinIO文件URL',
    `description` TEXT DEFAULT NULL COMMENT '文档描述',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户文档表';

-- 创建初始管理员用户
INSERT INTO `user` (`username`, `password`, `phone`, `email`, `balance`, `status`, `role`, `create_time`, `update_time`) 
VALUES ('admin', '123456', '13800138000', 'admin@example.com', 0.00, 1, 1, NOW(), NOW());

-- 注意：实际应用中，密码应该使用加密存储，例如：
-- INSERT INTO `user` (`username`, `password`, `phone`, `email`, `balance`, `status`, `role`, `create_time`, `update_time`) 
-- VALUES ('admin', '$2a$10$X/XQzGZ1xfJP.3xEyxhQv.Ib1QD7wLSs1LzO7hUW1UgSBf5E3XlLe', '13800138000', 'admin@example.com', 0.00, 1, 1, NOW(), NOW()); 