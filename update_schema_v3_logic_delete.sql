-- 数据库变更脚本 v3.1
-- 目标：补齐逻辑删除字段，统一删除语义
-- 适用：MySQL 5.7+ / MySQL 8.0+
-- 说明：不用 ADD COLUMN IF NOT EXISTS，兼容低版本 MySQL

SET @schema_name = DATABASE();

-- 1) 补齐 is_deleted 字段（幂等，兼容低版本）
SET @exists_col = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'activities'
      AND COLUMN_NAME = 'is_deleted'
);
SET @sql = IF(
    @exists_col = 0,
    'ALTER TABLE `activities` ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_col = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'comment'
      AND COLUMN_NAME = 'is_deleted'
);
SET @sql = IF(
    @exists_col = 0,
    'ALTER TABLE `comment` ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_col = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'comment_like'
      AND COLUMN_NAME = 'is_deleted'
);
SET @sql = IF(
    @exists_col = 0,
    'ALTER TABLE `comment_like` ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_col = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'honor_period'
      AND COLUMN_NAME = 'is_deleted'
);
SET @sql = IF(
    @exists_col = 0,
    'ALTER TABLE `honor_period` ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists_col = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'honor_period_item'
      AND COLUMN_NAME = 'is_deleted'
);
SET @sql = IF(
    @exists_col = 0,
    'ALTER TABLE `honor_period_item` ADD COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT ''逻辑删除''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 历史 NULL 数据清洗为 0
UPDATE `activities` SET `is_deleted` = 0 WHERE `is_deleted` IS NULL;
UPDATE `comment` SET `is_deleted` = 0 WHERE `is_deleted` IS NULL;
UPDATE `comment_like` SET `is_deleted` = 0 WHERE `is_deleted` IS NULL;
UPDATE `honor_period` SET `is_deleted` = 0 WHERE `is_deleted` IS NULL;
UPDATE `honor_period_item` SET `is_deleted` = 0 WHERE `is_deleted` IS NULL;

-- 3) 强制默认值与非空约束（防止后续脏数据）
ALTER TABLE `activities`
    MODIFY COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

ALTER TABLE `comment`
    MODIFY COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

ALTER TABLE `comment_like`
    MODIFY COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

ALTER TABLE `honor_period`
    MODIFY COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

ALTER TABLE `honor_period_item`
    MODIFY COLUMN `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- 4) 说明：operation_log 保持物理日志策略（当前无删除接口），不新增 is_deleted。
