-- 数据库变更脚本 v4.0
-- 目标：新增认领闭环与站内通知能力
-- 适用：MySQL 5.7+ / 8.0+

CREATE TABLE IF NOT EXISTS `claim_order` (
    `id` VARCHAR(64) NOT NULL COMMENT '认领单ID',
    `item_type` VARCHAR(20) NOT NULL COMMENT '信息类型(lost/find)',
    `item_id` VARCHAR(64) NOT NULL COMMENT '信息ID',
    `item_name` VARCHAR(100) DEFAULT NULL COMMENT '物品名称快照',
    `publisher_user_id` VARCHAR(64) NOT NULL COMMENT '发布者用户ID',
    `applicant_user_id` VARCHAR(64) NOT NULL COMMENT '申请者用户ID',
    `apply_note` VARCHAR(200) DEFAULT NULL COMMENT '申请说明',
    `proof_text` VARCHAR(500) DEFAULT NULL COMMENT '凭证说明',
    `proof_images` TEXT COMMENT '凭证图片JSON',
    `status` VARCHAR(32) NOT NULL DEFAULT 'APPLIED' COMMENT '状态(APPLIED/PROOF_SUBMITTED/CONFIRMED/COMPLETED/REJECTED)',
    `reject_reason` VARCHAR(200) DEFAULT NULL COMMENT '驳回原因',
    `apply_time` DATETIME DEFAULT NULL COMMENT '申请时间',
    `proof_submit_time` DATETIME DEFAULT NULL COMMENT '凭证提交时间',
    `confirm_time` DATETIME DEFAULT NULL COMMENT '发布者确认时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成归还时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_claim_item` (`item_type`, `item_id`),
    KEY `idx_claim_applicant` (`applicant_user_id`),
    KEY `idx_claim_publisher` (`publisher_user_id`),
    KEY `idx_claim_status` (`status`),
    KEY `idx_claim_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认领闭环流程单';

CREATE TABLE IF NOT EXISTS `user_notice` (
    `id` VARCHAR(64) NOT NULL COMMENT '通知ID',
    `user_id` VARCHAR(64) NOT NULL COMMENT '接收用户ID',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
    `biz_type` VARCHAR(32) NOT NULL DEFAULT 'SYSTEM' COMMENT '业务类型(AUDIT/COMMENT/CLAIM/SYSTEM)',
    `biz_id` VARCHAR(64) DEFAULT NULL COMMENT '关联业务ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读(0未读 1已读)',
    `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_notice_user_read` (`user_id`, `is_read`),
    KEY `idx_notice_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

-- 字典补充（幂等）
INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`, `is_deleted`)
SELECT 1, '已申请', 'APPLIED', 'claim_order_status', NULL, 'warning', 'N', '0', 'admin', NOW(), '认领单状态', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='claim_order_status' AND `dict_value`='APPLIED' AND COALESCE(`is_deleted`,0)=0
);

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`, `is_deleted`)
SELECT 2, '已提交凭证', 'PROOF_SUBMITTED', 'claim_order_status', NULL, 'primary', 'N', '0', 'admin', NOW(), '认领单状态', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='claim_order_status' AND `dict_value`='PROOF_SUBMITTED' AND COALESCE(`is_deleted`,0)=0
);

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`, `is_deleted`)
SELECT 3, '已确认', 'CONFIRMED', 'claim_order_status', NULL, 'success', 'N', '0', 'admin', NOW(), '认领单状态', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='claim_order_status' AND `dict_value`='CONFIRMED' AND COALESCE(`is_deleted`,0)=0
);

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`, `is_deleted`)
SELECT 4, '已完成', 'COMPLETED', 'claim_order_status', NULL, 'success', 'N', '0', 'admin', NOW(), '认领单状态', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='claim_order_status' AND `dict_value`='COMPLETED' AND COALESCE(`is_deleted`,0)=0
);

INSERT INTO `sys_dict_data`
(`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`, `is_deleted`)
SELECT 5, '已驳回', 'REJECTED', 'claim_order_status', NULL, 'danger', 'N', '0', 'admin', NOW(), '认领单状态', 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='claim_order_status' AND `dict_value`='REJECTED' AND COALESCE(`is_deleted`,0)=0
);

