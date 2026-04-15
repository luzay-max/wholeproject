INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `is_deleted`)
SELECT '管理控制台菜单', 'admin_console_menu', '0', '控制管理控制台菜单显示、排序与面包屑标题', 'system', NOW(), 'system', NOW(), 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'admin_console_menu' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 10, '信息管理中心', '/admin/info', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"List","contexts":["sidebar","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/info' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 20, '管理看板', '/admin/dashboard', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"DataAnalysis","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/dashboard' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 30, '用户管理', '/admin/users', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"User","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/users' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 40, '白名单管理', '/admin/whitelist', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"DocumentChecked","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/whitelist' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 50, '数据字典', '/admin/dict', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"Collection","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/dict' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 60, '评论管理', '/admin/comments', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"ChatDotRound","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/comments' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 70, '光荣榜管理', '/admin/honor', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"Trophy","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/honor' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 80, '活动日志', '/admin/activities', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"Operation","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/activities' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 90, '操作日志', '/admin/logs', 'admin_console_menu', NULL, 'primary', 'Y', '0', 'system', NOW(), 'system', NOW(), '{"icon":"Monitor","contexts":["sidebar","dropdown","mobile","breadcrumb"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/logs' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 100, '信息审核', '/admin/info?tab=audit', 'admin_console_menu', NULL, 'primary', 'N', '0', 'system', NOW(), 'system', NOW(), '{"icon":"List","contexts":["dropdown","mobile"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/info?tab=audit' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 110, '失物管理', '/admin/info?tab=manage&type=lost', 'admin_console_menu', NULL, 'primary', 'N', '0', 'system', NOW(), 'system', NOW(), '{"icon":"List","contexts":["dropdown","mobile"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/info?tab=manage&type=lost' AND `is_deleted` = 0
);

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `is_deleted`)
SELECT 120, '招领管理', '/admin/info?tab=manage&type=find', 'admin_console_menu', NULL, 'primary', 'N', '0', 'system', NOW(), 'system', NOW(), '{"icon":"List","contexts":["dropdown","mobile"]}', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type` = 'admin_console_menu' AND `dict_value` = '/admin/info?tab=manage&type=find' AND `is_deleted` = 0
);
