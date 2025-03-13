-- 用户表
CREATE TABLE `t_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 1:正常 0:禁用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户请求统计表
CREATE TABLE `t_request_stats` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `request_date` date NOT NULL COMMENT '统计日期',
    `request_count` int(11) NOT NULL DEFAULT '0' COMMENT '请求次数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_date` (`user_id`,`request_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户请求统计表';

-- 用户知识库表
CREATE TABLE `t_knowledge_base` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `product_id` bigint(20) NOT NULL COMMENT '产品ID',
    `product_name` varchar(100) NOT NULL COMMENT '产品名称',
    `file_name` varchar(200) NOT NULL COMMENT '文件名称',
    `file_path` varchar(500) NOT NULL COMMENT 'MinIO文件路径',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 1:正常 0:删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户知识库表';

-- 产品表
CREATE TABLE `t_product` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `product_name` varchar(100) NOT NULL COMMENT '产品名称',
    `description` varchar(500) DEFAULT NULL COMMENT '产品描述',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 1:正常 0:删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- 用户权益表
CREATE TABLE `t_user_balance` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '账户余额',
    `total_recharge` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '总充值金额',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权益表';

-- 充值记录表
CREATE TABLE `t_recharge_record` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `amount` decimal(10,2) NOT NULL COMMENT '充值金额',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态 0:处理中 1:成功 2:失败',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- 知识片段表
CREATE TABLE `t_knowledge_fragment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `knowledge_id` bigint(20) NOT NULL COMMENT '知识库ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `product_id` bigint(20) NOT NULL COMMENT '产品ID',
    `question` text NOT NULL COMMENT '问题',
    `answer` text NOT NULL COMMENT '答案',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态 1:正常 0:删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_knowledge_id` (`knowledge_id`),
    KEY `idx_user_product` (`user_id`,`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识片段表'; 