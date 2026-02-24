-- 修复 is_deleted 字段为 NULL 导致无法查询数据的问题
-- MyBatis Plus 的 @TableLogic 默认会查询 is_deleted = 0 的数据
-- 如果 is_deleted 为 NULL，则不会被查出来

UPDATE `sys_dict_data` SET `is_deleted` = 0 WHERE `is_deleted` IS NULL;
UPDATE `sys_dict_type` SET `is_deleted` = 0 WHERE `is_deleted` IS NULL;

-- 确保后续插入的数据 is_deleted 为 0
ALTER TABLE `sys_dict_data` MODIFY COLUMN `is_deleted` int(1) DEFAULT 0;
ALTER TABLE `sys_dict_type` MODIFY COLUMN `is_deleted` int(1) DEFAULT 0;

-- 重新插入可能缺失的数据（带上 is_deleted = 0）
-- info_status
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `is_deleted`, `create_by`, `create_time`) 
SELECT '物品状态', 'info_status', '0', 0, 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'info_status');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 1, '寻找中', '0', 'info_status', '0', 0, 'warning', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_status' AND `dict_value` = '0');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 2, '已寻回', '1', 'info_status', '0', 0, 'success', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_status' AND `dict_value` = '1');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 3, '已认领', '2', 'info_status', '0', 0, 'success', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_status' AND `dict_value` = '2');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 4, '已取消', '3', 'info_status', '0', 0, 'info', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_status' AND `dict_value` = '3');

-- info_type
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `is_deleted`, `create_by`, `create_time`) 
SELECT '物品类型', 'info_type', '0', 0, 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'info_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 1, '电子产品', '1', 'info_type', '0', 0, '', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_type' AND `dict_value` = '1');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 2, '生活用品', '2', 'info_type', '0', 0, '', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_type' AND `dict_value` = '2');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 3, '证件', '3', 'info_type', '0', 0, '', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_type' AND `dict_value` = '3');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `create_by`, `create_time`) 
SELECT 4, '其他', '4', 'info_type', '0', 0, '', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'info_type' AND `dict_value` = '4');

-- sys_user_role
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `is_deleted`, `create_by`, `create_time`) 
SELECT '用户角色', 'sys_user_role', '0', 0, 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'sys_user_role');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `remark`, `create_by`, `create_time`) 
SELECT 1, '学生', 'STUDENT', 'sys_user_role', '0', 0, 'primary', '我是在校学生', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'sys_user_role' AND `dict_value` = 'STUDENT');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `is_deleted`, `list_class`, `remark`, `create_by`, `create_time`) 
SELECT 2, '教职工', 'TEACHER', 'sys_user_role', '0', 0, 'success', '我是教职员工', 'admin', NOW() FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'sys_user_role' AND `dict_value` = 'TEACHER');

