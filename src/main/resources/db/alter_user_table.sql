-- 添加角色字段到用户表
ALTER TABLE `user` 
ADD COLUMN `role` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '用户角色：0-普通用户，1-管理员' AFTER `status`;

-- 更新现有用户为普通用户
UPDATE `user` SET `role` = 0 WHERE `role` IS NULL; 