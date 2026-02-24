
-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT IGNORE INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`, `create_by`, `create_time`) VALUES 
('用户性别', 'sys_user_sex', '0', '用户性别列表', 'admin', NOW()),
('菜单状态', 'sys_show_hide', '0', '菜单状态列表', 'admin', NOW()),
('系统开关', 'sys_normal_disable', '0', '系统开关列表', 'admin', NOW()),
('任务状态', 'sys_job_status', '0', '任务状态列表', 'admin', NOW()),
('任务分组', 'sys_job_group', '0', '任务分组列表', 'admin', NOW()),
('系统是否', 'sys_yes_no', '0', '系统是否列表', 'admin', NOW()),
('通知类型', 'sys_notice_type', '0', '通知类型列表', 'admin', NOW()),
('通知状态', 'sys_notice_status', '0', '通知状态列表', 'admin', NOW()),
('操作类型', 'sys_oper_type', '0', '操作类型列表', 'admin', NOW()),
('操作状态', 'sys_common_status', '0', '操作状态列表', 'admin', NOW()),
('物品状态', 'info_status', '0', '失物招领物品状态', 'admin', NOW()),
('物品类型', 'info_type', '0', '失物招领物品类型', 'admin', NOW()),
('审核状态', 'audit_status', '0', '审核状态列表', 'admin', NOW()),
('荣誉榜状态', 'honor_status', '0', '荣誉榜显示状态', 'admin', NOW()),
('日志操作类型', 'sys_oper_type', '0', '系统操作日志类型', 'admin', NOW()),
('日志操作状态', 'sys_oper_status', '0', '系统操作结果状态', 'admin', NOW()),
('活动操作类型', 'activity_action', '0', '活动日志操作类型', 'admin', NOW()),
('活动物品类型', 'activity_item_type', '0', '活动日志物品类型', 'admin', NOW()),
('用户角色', 'sys_user_role', '0', '系统用户角色', 'admin', NOW()),
('用户状态', 'sys_user_status', '0', '系统用户状态', 'admin', NOW());

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(4) NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `is_deleted` int(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`dict_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 100 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT IGNORE INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `status`, `remark`, `list_class`, `create_by`, `create_time`) VALUES 
(1, '男', '0', 'sys_user_sex', '0', NULL, '', 'admin', NOW()),
(2, '女', '1', 'sys_user_sex', '0', NULL, '', 'admin', NOW()),
(3, '未知', '2', 'sys_user_sex', '0', NULL, '', 'admin', NOW()),
(1, '显示', '0', 'sys_show_hide', '0', NULL, 'primary', 'admin', NOW()),
(2, '隐藏', '1', 'sys_show_hide', '0', NULL, 'danger', 'admin', NOW()),
(1, '正常', '0', 'sys_normal_disable', '0', NULL, 'primary', 'admin', NOW()),
(2, '停用', '1', 'sys_normal_disable', '0', NULL, 'danger', 'admin', NOW()),
(1, '正常', '0', 'sys_job_status', '0', NULL, 'primary', 'admin', NOW()),
(2, '暂停', '1', 'sys_job_status', '0', NULL, 'danger', 'admin', NOW()),
(1, '默认', 'DEFAULT', 'sys_job_group', '0', NULL, '', 'admin', NOW()),
(2, '系统', 'SYSTEM', 'sys_job_group', '0', NULL, '', 'admin', NOW()),
(1, '是', 'Y', 'sys_yes_no', '0', NULL, 'primary', 'admin', NOW()),
(2, '否', 'N', 'sys_yes_no', '0', NULL, 'danger', 'admin', NOW()),
(1, '通知', '1', 'sys_notice_type', '0', NULL, 'warning', 'admin', NOW()),
(2, '公告', '2', 'sys_notice_type', '0', NULL, 'success', 'admin', NOW()),
(1, '正常', '0', 'sys_notice_status', '0', NULL, 'primary', 'admin', NOW()),
(2, '关闭', '1', 'sys_notice_status', '0', NULL, 'danger', 'admin', NOW()),
(1, '新增', '1', 'sys_oper_type', '0', NULL, 'info', 'admin', NOW()),
(2, '修改', '2', 'sys_oper_type', '0', NULL, 'info', 'admin', NOW()),
(3, '删除', '3', 'sys_oper_type', '0', NULL, 'danger', 'admin', NOW()),
(4, '授权', '4', 'sys_oper_type', '0', NULL, 'primary', 'admin', NOW()),
(5, '导出', '5', 'sys_oper_type', '0', NULL, 'warning', 'admin', NOW()),
(6, '导入', '6', 'sys_oper_type', '0', NULL, 'warning', 'admin', NOW()),
(7, '强退', '7', 'sys_oper_type', '0', NULL, 'danger', 'admin', NOW()),
(8, '生成代码', '8', 'sys_oper_type', '0', NULL, 'warning', 'admin', NOW()),
(9, '清空数据', '9', 'sys_oper_type', '0', NULL, 'danger', 'admin', NOW()),
(1, '成功', '0', 'sys_common_status', '0', NULL, 'primary', 'admin', NOW()),
(2, '失败', '1', 'sys_common_status', '0', NULL, 'danger', 'admin', NOW()),

-- 物品类型 (info_type) - Updated
(1, '证件', 'ID_CARD', 'info_type', '0', NULL, 'primary', 'admin', NOW()),
(2, '电子产品', 'ELECTRONICS', 'info_type', '0', NULL, 'success', 'admin', NOW()),
(3, '书籍文具', 'BOOKS', 'info_type', '0', NULL, 'info', 'admin', NOW()),
(4, '衣物', 'CLOTHES', 'info_type', '0', NULL, 'warning', 'admin', NOW()),
(5, '生活用品', 'DAILY_NECESSITIES', 'info_type', '0', NULL, 'success', 'admin', NOW()),
(6, '其他', 'OTHER', 'info_type', '0', NULL, 'info', 'admin', NOW()),

-- 物品状态 (info_status) - Updated
(1, '待审核', 'PENDING', 'info_status', '0', NULL, 'warning', 'admin', NOW()),
(2, '已发布', 'APPROVED', 'info_status', '0', NULL, 'success', 'admin', NOW()),
(3, '已拒绝', 'REJECTED', 'info_status', '0', NULL, 'danger', 'admin', NOW()),
(4, '已解决', 'SOLVED', 'info_status', '0', NULL, 'info', 'admin', NOW()),

-- 用户角色 (sys_user_role) - Updated
(1, '学生', 'STUDENT', 'sys_user_role', '0', '我是在校学生', 'primary', 'admin', NOW()),
(2, '教职工', 'TEACHER', 'sys_user_role', '0', '我是教职员工', 'success', 'admin', NOW()),
(3, '管理员', 'ADMIN', 'sys_user_role', '0', '系统管理员', 'danger', 'admin', NOW()),

-- 用户状态 (sys_user_status)
(1, '正常', '0', 'sys_user_status', '0', NULL, 'primary', 'admin', NOW()),
(2, '封禁', '1', 'sys_user_status', '0', NULL, 'danger', 'admin', NOW()),

-- Other existing dicts
(1, '待审核', '0', 'audit_status', '0', NULL, 'warning', 'admin', NOW()),
(2, '已通过', '1', 'audit_status', '0', NULL, 'success', 'admin', NOW()),
(3, '已驳回', '2', 'audit_status', '0', NULL, 'danger', 'admin', NOW()),
(1, '显示', '1', 'honor_status', '0', NULL, 'success', 'admin', NOW()),
(2, '隐藏', '0', 'honor_status', '0', NULL, 'info', 'admin', NOW()),
(1, '登录', 'LOGIN', 'sys_oper_type', '0', NULL, 'info', 'admin', NOW()),
(2, '注册', 'REGISTER', 'sys_oper_type', '0', NULL, 'success', 'admin', NOW()),
(3, '查询', 'QUERY', 'sys_oper_type', '0', NULL, 'primary', 'admin', NOW()),
(4, '新增', 'ADD', 'sys_oper_type', '0', NULL, 'success', 'admin', NOW()),
(5, '修改', 'UPDATE', 'sys_oper_type', '0', NULL, 'warning', 'admin', NOW()),
(6, '删除', 'DELETE', 'sys_oper_type', '0', NULL, 'danger', 'admin', NOW()),
(7, '授权', 'GRANT', 'sys_oper_type', '0', NULL, 'primary', 'admin', NOW()),
(8, '导出', 'EXPORT', 'sys_oper_type', '0', NULL, 'info', 'admin', NOW()),
(9, '导入', 'IMPORT', 'sys_oper_type', '0', NULL, 'info', 'admin', NOW()),
(10, '其他', 'OTHER', 'sys_oper_type', '0', NULL, 'info', 'admin', NOW()),
(1, '成功', '0', 'sys_oper_status', '0', NULL, 'success', 'admin', NOW()),
(2, '失败', '1', 'sys_oper_status', '0', NULL, 'danger', 'admin', NOW()),
(1, '发布', 'publish', 'activity_action', '0', NULL, 'primary', 'admin', NOW()),
(2, '解决', 'UPDATE_STATUS', 'activity_action', '0', NULL, 'success', 'admin', NOW()),
(3, '评论', 'comment', 'activity_action', '0', NULL, 'info', 'admin', NOW()),
(1, '失物信息', 'lost', 'activity_item_type', '0', NULL, 'warning', 'admin', NOW()),
(2, '招领信息', 'find', 'activity_item_type', '0', NULL, 'success', 'admin', NOW());
