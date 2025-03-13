-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `balance` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '账户余额',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-正常',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 充值记录表
CREATE TABLE IF NOT EXISTS `recharge_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
    `amount` DECIMAL(10, 2) NOT NULL COMMENT '充值金额',
    `before_balance` DECIMAL(10, 2) NOT NULL COMMENT '充值前余额',
    `after_balance` DECIMAL(10, 2) NOT NULL COMMENT '充值后余额',
    `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '充值状态：0-失败，1-成功',
    `pay_type` TINYINT(4) NOT NULL COMMENT '充值方式：1-支付宝，2-微信，3-银行卡',
    `trade_no` VARCHAR(64) DEFAULT NULL COMMENT '交易流水号',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表'; 