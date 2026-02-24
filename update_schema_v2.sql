-- 数据库变更脚本 v2.0
-- 说明: 请在您的数据库管理工具 (如 Navicat, Workbench) 中运行此脚本
-- 注意: 运行前请确保已选中正确的数据库 (如 use lost_and_found;)

-- 1. 用户表新增字段
-- account_name: 账户名 (用于登录, 不可改)
-- student_id: 学号 (用于白名单校验)
-- college: 学院
-- status: 状态 (0正常, 1封禁)
-- is_deleted: 逻辑删除
ALTER TABLE `user` ADD COLUMN `account_name` VARCHAR(50) UNIQUE COMMENT '账户名';
ALTER TABLE `user` ADD COLUMN `student_id` VARCHAR(50) UNIQUE COMMENT '学号';
ALTER TABLE `user` ADD COLUMN `college` VARCHAR(100) COMMENT '学院';
ALTER TABLE `user` ADD COLUMN `status` TINYINT DEFAULT 0 COMMENT '状态 0:正常 1:封禁';
ALTER TABLE `user` ADD COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除';

-- 数据迁移: 初始化 account_name = username (防止唯一索引报错，建议先执行此行再创建唯一索引，但此处为新增列，默认为 NULL，需注意)
-- 修正: 由于 UNIQUE 约束，新增列默认为 NULL 是允许的。
-- 建议运行完 ALTER 后，执行: UPDATE `user` SET `account_name` = `username` WHERE `account_name` IS NULL;

-- 2. 失物表新增字段
ALTER TABLE `lost_info` ADD COLUMN `lost_time` DATETIME COMMENT '丢失时间';
ALTER TABLE `lost_info` ADD COLUMN `contact_info` VARCHAR(100) COMMENT '联系方式';
ALTER TABLE `lost_info` ADD COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除';

-- 3. 招领表新增字段
ALTER TABLE `find_info` ADD COLUMN `found_time` DATETIME COMMENT '捡到时间';
ALTER TABLE `find_info` ADD COLUMN `contact_info` VARCHAR(100) COMMENT '联系方式';
ALTER TABLE `find_info` ADD COLUMN `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除';

-- 4. 新增日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT COMMENT '操作人',
    `action_type` VARCHAR(50) COMMENT '操作类型',
    `content` TEXT COMMENT '详情',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT='操作日志表';

-- 5. 新增白名单表
CREATE TABLE IF NOT EXISTS `student_whitelist` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `student_id` VARCHAR(50) UNIQUE COMMENT '学号',
    `name` VARCHAR(50) COMMENT '姓名',
    `college` VARCHAR(100) COMMENT '学院',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` INT DEFAULT 0 COMMENT '逻辑删除'
) COMMENT='学生白名单表';

-- 6. 数据初始化 (可选)
-- 将现有用户的 account_name 填充为 username
UPDATE `user` SET `account_name` = `username` WHERE `account_name` IS NULL;
