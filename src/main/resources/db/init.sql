-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE test;

-- 产品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `name` varchar(100) NOT NULL COMMENT '产品名称',
  `price` decimal(10,2) NOT NULL COMMENT '产品价格',
  `description` text DEFAULT NULL COMMENT '产品描述',
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '库存数量',
  `category` varchar(50) DEFAULT NULL COMMENT '产品类别',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- 插入测试数据
INSERT INTO `product` (`name`, `price`, `description`, `stock`, `category`, `status`) VALUES 
('测试产品1', 99.99, '这是测试产品1的详细描述', 100, '电子产品', 1),
('测试产品2', 199.99, '这是测试产品2的详细描述', 50, '家居用品', 1),
('测试产品3', 299.99, '这是测试产品3的详细描述', 30, '电子产品', 1); 