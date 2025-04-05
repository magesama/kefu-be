-- 创建产品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `name` varchar(100) NOT NULL COMMENT '产品名称',
  `description` text COMMENT '产品描述',
  `price` decimal(10,2) DEFAULT '0.00' COMMENT '产品价格',
  `stock` int(11) DEFAULT '0' COMMENT '产品库存',
  `category` varchar(50) DEFAULT NULL COMMENT '产品分类',
  `status` tinyint(4) DEFAULT '1' COMMENT '产品状态：0-下架，1-上架',
  `is_deleted` tinyint(4) DEFAULT '0' COMMENT '删除标识：0-未删除，1-已删除',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表'; 