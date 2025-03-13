-- 添加产品和店铺相关字段
ALTER TABLE `user_document`
    ADD COLUMN `product_name` VARCHAR(255) DEFAULT NULL COMMENT '产品名称' AFTER `product_id`,
    ADD COLUMN `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID' AFTER `product_name`,
    ADD COLUMN `shop_name` VARCHAR(255) DEFAULT NULL COMMENT '店铺名称' AFTER `shop_id`; 