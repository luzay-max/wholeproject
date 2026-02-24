/*
 Navicat Premium Dump SQL

 Source Server         : test
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : lose_and_found

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 04/02/2026 11:35:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activities
-- ----------------------------
DROP TABLE IF EXISTS `activities`;
CREATE TABLE `activities`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '动态ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型（发布/解决/评论）',
  `item_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品类型（lost/find）',
  `item_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '动态内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`, `action`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_type_time`(`item_type` ASC, `create_time` ASC) USING BTREE,
  CONSTRAINT `fk_activities_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户活动日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activities
-- ----------------------------
INSERT INTO `activities` VALUES ('02c6672c-6904-4ddb-8802-7eb40dd87b65', '1987755992376414210', 'PUBLISH', 'lost', 'd12fe219-3feb-4604-87c5-7e8e44b381a5', '发布了失物信息：透明白色杯子', '2025-11-12 16:14:28');
INSERT INTO `activities` VALUES ('0dd884e3-66cb-472a-b740-02762d7ebcdd', '1979079350726078466', 'UPDATE_STATUS', 'lost', 'b5f320c0-c613-4e0e-95c3-2d56b440a52d', '失物驱蚊器已找回', '2025-11-12 15:37:21');
INSERT INTO `activities` VALUES ('2ae6f18e-fdda-4eff-8ae8-bab3c37f8204', '1979079350726078466', 'UPDATE_STATUS', 'find', '2718a9cf-4c3a-4558-a980-3e0c5dbcfa93', '招领物品:哈基米已认领', '2025-11-12 15:39:03');
INSERT INTO `activities` VALUES ('2d9fdca3-21dc-440c-9977-062695f9cd8a', '1979079350726078466', 'PUBLISH', 'lost', '7040a0f7-c54a-4828-8e06-f81eab7cdf67', '发布了失物信息：手机', '2025-12-18 19:54:39');
INSERT INTO `activities` VALUES ('31869d6d-9d97-4c65-b816-a0dcf6ba28af', '1979079350726078466', 'PUBLISH', 'lost', 'ef7bca95-736c-490f-926d-52e6f4aae6d0', '发布了失物信息：11111111', '2026-02-04 11:23:14');
INSERT INTO `activities` VALUES ('362c2662-f9d4-4fe2-a65a-620d216bab0a', '1982720631266926594', 'UPDATE_STATUS', 'find', '5b286573-1362-484e-a649-bda032985a88', '将招领信息《啊实打实大大》状态更新为：SOLVED', '2025-11-05 14:59:37');
INSERT INTO `activities` VALUES ('3a75f21c-c6eb-445a-96cb-745ccb6624cc', '1979079350726078466', 'PUBLISH', 'find', '2718a9cf-4c3a-4558-a980-3e0c5dbcfa93', '发布了招领信息：哈基米', '2025-11-09 14:58:22');
INSERT INTO `activities` VALUES ('3e4ac793-e917-43b5-b1f0-6b61513a93e2', '1979079350726078466', 'PUBLISH', 'find', '5628a7c2-b5a2-4e45-8745-fb996f08ab32', '发布了招领信息：手机', '2026-01-16 09:29:13');
INSERT INTO `activities` VALUES ('422c679b-1166-464b-aa54-d9b50df94177', '2018250806839341057', 'PUBLISH', 'find', 'ca018f5d-1d03-4209-915c-b22faf7ea6be', '发布了招领信息：1111', '2026-02-03 17:20:48');
INSERT INTO `activities` VALUES ('45b831ed-d137-4565-b5df-0d925cca7453', '1979079350726078466', 'PUBLISH', 'lost', '015b0a13-e664-4c09-9c54-5a31eded8e84', '发布了失物信息：校服', '2025-11-19 16:15:55');
INSERT INTO `activities` VALUES ('45df21bf-5421-4e4f-bf1c-8d9edeeda5af', '1982720631266926594', 'PUBLISH', 'find', '1bab60b9-90dc-46fb-9ca1-38fbf86337c5', '发布了招领信息：智能手机', '2025-11-12 15:58:08');
INSERT INTO `activities` VALUES ('46077968-a2b4-4154-b3c6-cbb464885bce', '1987755992376414210', 'PUBLISH', 'lost', '0e39a4fa-ec9a-45b8-87f8-277aba1581d6', '发布了失物信息：11111', '2026-01-31 16:49:16');
INSERT INTO `activities` VALUES ('4a10cc2a-b7ad-442a-b20c-19d3e1649595', '1979079350726078466', 'UPDATE_STATUS', 'find', '6a63bdb3-97d4-4a27-96fd-5e3ace4d1e22', '招领物品:背包已认领', '2026-01-15 14:08:24');
INSERT INTO `activities` VALUES ('4cc0aecb-ff07-4c73-a9f3-067ba45e10e5', '1982720631266926594', 'PUBLISH', 'find', '76d69bc9-fb58-4dca-a5fa-bfeda3021d3b', '发布了招领信息：实打实', '2025-11-05 15:01:33');
INSERT INTO `activities` VALUES ('50a82fc1-c85e-46c5-b720-49fec1303c33', '1979079350726078466', 'PUBLISH', 'lost', '64111d69-1676-4a9e-9de8-8ade9f6ac97e', '发布了失物信息：11111111111', '2026-02-04 11:24:04');
INSERT INTO `activities` VALUES ('5768da50-a80a-4a17-9e0e-adb4c8c88ef6', '1979079350726078466', 'PUBLISH', 'lost', 'a9edc73c-4456-4549-8da7-f8f3c1b89635', '发布了失物信息：nnmnn', '2025-12-18 10:55:54');
INSERT INTO `activities` VALUES ('590430ae-7e17-44ad-96e7-f4d68837a0a9', '1979079350726078466', 'UPDATE_STATUS', 'lost', '015b0a13-e664-4c09-9c54-5a31eded8e84', '失物校服已找回', '2026-01-15 14:08:34');
INSERT INTO `activities` VALUES ('5ce4f914-0e35-4f57-aee4-3fa66ab032fb', '1987755992376414210', 'UPDATE_STATUS', 'find', '46271170-7045-4d80-ad4e-c7e485628622', '招领物品:1111111111111已认领', '2026-02-02 10:23:12');
INSERT INTO `activities` VALUES ('65db07b8-53ce-47ba-a194-232266425f61', '1979079350726078466', 'UPDATE_STATUS', 'find', 'd63d5ed2-71a6-4127-a84c-04d2ebd11419', '招领物品:啊实打实大已认领', '2025-11-12 15:38:47');
INSERT INTO `activities` VALUES ('67d9ddb6-ae3d-4d0c-a031-f9c30b26e0a6', '1979079350726078466', 'PUBLISH', 'lost', '1d361a31-1bc4-4645-a404-54003e517888', '发布了失物信息：1111111', '2026-01-22 17:23:42');
INSERT INTO `activities` VALUES ('6b06aa42-c13d-4380-84f0-e367847baeee', '1979079350726078466', 'PUBLISH', 'lost', '4abd0ba1-c5c0-4ed0-b43b-ecc7acf308d2', '发布了失物信息：伞', '2025-11-12 15:54:07');
INSERT INTO `activities` VALUES ('6b69a9be-a294-4034-ab27-f95ec10fe466', '1982720631266926594', 'UPDATE_STATUS', 'find', '5b286573-1362-484e-a649-bda032985a88', '招领物品:啊实打实大大已认领', '2025-11-12 15:39:53');
INSERT INTO `activities` VALUES ('6c21712a-1657-4e9a-8c8f-f0f69582be04', '1979079350726078466', 'PUBLISH', 'find', '7ac6a6cd-a7b4-48b8-8a25-8af6517b0266', '发布了招领信息：雨衣', '2026-02-02 13:55:43');
INSERT INTO `activities` VALUES ('6f1a8939-d3e5-4898-b7c6-1a141f4057fd', '1987755992376414210', 'UPDATE_STATUS', 'lost', 'd12fe219-3feb-4604-87c5-7e8e44b381a5', '失物透明白色杯子已找回', '2025-12-22 16:46:14');
INSERT INTO `activities` VALUES ('724ecfcf-be34-4d19-8d95-9660ca4c3731', '1979079350726078466', 'UPDATE_STATUS', 'lost', '4cfee812-c7b6-40ac-bd19-747d55620489', '失物小米已找回', '2025-11-10 13:58:30');
INSERT INTO `activities` VALUES ('76363e4b-7154-4d02-9b43-e750f97bb78f', '1982720631266926594', 'UPDATE_STATUS', 'lost', 'e563bd0d-3e57-42f2-ad27-9eb7c0653136', '失物啊实打实大师大已找回', '2025-11-12 15:39:43');
INSERT INTO `activities` VALUES ('76869ced-b909-480a-aa90-c9df20a839ec', '1987755992376414210', 'PUBLISH', 'find', '0d280d4a-4ed6-435b-a077-9c281266638f', '发布了招领信息：111', '2025-11-10 13:38:36');
INSERT INTO `activities` VALUES ('7b125b60-290e-4929-8a5c-72497f6d0a49', '1987755992376414210', 'PUBLISH', 'find', 'f0bd336a-47cf-4489-b729-4cddc6295d7f', '发布了招领信息：1111111', '2026-01-31 16:49:15');
INSERT INTO `activities` VALUES ('82e8fb3a-6abc-4282-8236-b66545a2d44d', '1979079350726078466', 'PUBLISH', 'lost', '4cfee812-c7b6-40ac-bd19-747d55620489', '发布了失物信息：小米', '2025-11-09 16:07:34');
INSERT INTO `activities` VALUES ('82ec70a9-b152-4a27-8816-2f149a123467', '1982720631266926594', 'UPDATE_STATUS', 'lost', 'f0a97937-de6b-40ee-b67c-f9dd7111d8b9', '失物衣服已找回', '2025-11-12 16:11:11');
INSERT INTO `activities` VALUES ('8e61b617-ffca-479d-b910-d196edfe6946', '1979079350726078466', 'PUBLISH', 'lost', '0c2213d2-a5d7-485e-afad-bf5cc53b63b7', '发布了失物信息：咋水电费', '2026-01-17 19:38:09');
INSERT INTO `activities` VALUES ('8f29f86e-3f3f-443a-bc5d-c9c229235650', '1982720631266926594', 'PUBLISH', 'lost', '37c02eb5-13bd-460d-9e46-b24fc6a30e7a', '发布了失物信息：钱包', '2025-11-05 15:17:28');
INSERT INTO `activities` VALUES ('8f4c3c68-ccae-41f7-b4c5-8e2741fe8a59', '1979079350726078466', 'UPDATE_STATUS', 'lost', '01998f14-3977-4be9-957a-7df729477362', '失物阿达阿达阿达已找回', '2025-11-12 15:38:16');
INSERT INTO `activities` VALUES ('9135ee7d-9bfe-4681-a3a0-f34cb2313d2c', '1987755992376414210', 'PUBLISH', 'find', '70620bf2-de33-4b47-bbd4-efe296eeb927', '发布了招领信息：111', '2026-01-28 10:43:19');
INSERT INTO `activities` VALUES ('91d78c95-5cd0-4c70-acd0-8af3f6672372', '1987755992376414210', 'UPDATE_STATUS', 'find', '0d280d4a-4ed6-435b-a077-9c281266638f', '招领物品:111已认领', '2025-11-12 15:40:27');
INSERT INTO `activities` VALUES ('9a776b98-3745-4834-b14e-48e45ff6b0aa', '1979079350726078466', 'UPDATE_STATUS', 'lost', '5fc5c14f-253a-44e3-8737-4419409c1b28', '失物啊实打实大已找回', '2025-11-12 15:38:32');
INSERT INTO `activities` VALUES ('a0bb0340-1c71-404c-8e38-bdbdb9ffa3e9', '1982720631266926594', 'PUBLISH', 'lost', 'f0a97937-de6b-40ee-b67c-f9dd7111d8b9', '发布了失物信息：衣服', '2025-11-12 15:58:08');
INSERT INTO `activities` VALUES ('a36f2939-1bca-4dc0-8d8a-aebf792831e5', '1979079350726078466', 'PUBLISH', 'find', '1bf9e57b-2b4f-4f34-b042-519a3975e111', '发布了招领信息：阿萨德', '2025-11-09 16:08:23');
INSERT INTO `activities` VALUES ('a6c56c94-71f4-4ece-abbe-e6983f04ad6e', '1987755992376414210', 'UPDATE_STATUS', 'lost', 'e7c92d53-0525-48eb-bb41-6867efcb44e6', '失物111111已找回', '2026-01-28 10:10:10');
INSERT INTO `activities` VALUES ('a7bec412-66dd-4cb4-a3c2-115d0061230c', '1979079350726078466', 'UPDATE_STATUS', 'find', '8f6bd347-7714-4e8b-b4cc-3c084e73dca0', '招领物品:太阳眼镜已认领', '2026-01-15 14:08:42');
INSERT INTO `activities` VALUES ('a96c9952-3231-48d7-ad44-0188c1138564', '1979079350726078466', 'PUBLISH', 'lost', 'fcc3cf76-997c-4022-b9a6-1654519ab56c', '发布了失物信息：保温杯', '2025-11-12 15:54:07');
INSERT INTO `activities` VALUES ('acf2a123-d7c7-4783-bf4c-a6c83abe095b', '1979079350726078466', 'PUBLISH', 'find', '5214310f-c908-470f-a06d-6d6468a75343', '发布了招领信息：111', '2026-02-04 11:30:08');
INSERT INTO `activities` VALUES ('b28629ef-8138-4aae-b07a-e4159198e49a', '1982720631266926594', 'UPDATE_STATUS', 'lost', 'f8dc65b0-3007-4f52-8a2b-0678b5f0d621', '失物阿斯达大厦已找回', '2025-11-12 15:39:33');
INSERT INTO `activities` VALUES ('b611e5f4-ab34-4014-b1bc-3d7bd39429b9', '1979079350726078466', 'UPDATE_STATUS', 'lost', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', '失物test已找回', '2025-11-12 15:37:16');
INSERT INTO `activities` VALUES ('b9cf66ae-844d-4614-b2a6-a67f4fb662a6', '1979079350726078466', 'UPDATE_STATUS', 'lost', 'd7e5be39-140c-4cd8-8ad0-4eff307523c3', '失物啊实打实大已找回', '2025-11-12 15:38:07');
INSERT INTO `activities` VALUES ('ba8d4340-e1f4-4878-b9cf-52f119e10724', '1982720631266926594', 'UPDATE_STATUS', 'lost', '37c02eb5-13bd-460d-9e46-b24fc6a30e7a', '失物钱包已找回', '2025-11-12 15:39:27');
INSERT INTO `activities` VALUES ('c3985417-565c-42e8-a81c-921787baf280', '1979079350726078466', 'UPDATE_STATUS', 'find', '1bf9e57b-2b4f-4f34-b042-519a3975e111', '招领物品:阿萨德已认领', '2025-11-11 13:56:05');
INSERT INTO `activities` VALUES ('cc537637-5cd9-4aa3-bde9-deaf2e0ef416', '1979079350726078466', 'PUBLISH', 'find', '8f6bd347-7714-4e8b-b4cc-3c084e73dca0', '发布了招领信息：太阳眼镜', '2025-11-12 15:54:07');
INSERT INTO `activities` VALUES ('cd94c627-21b3-4198-becc-69623a8cdb47', '2018165850192605186', 'PUBLISH', 'find', '42686947-e680-469e-8dbe-5aa4959e1ad6', '发布了招领信息：粉色袜子', '2026-02-02 13:55:42');
INSERT INTO `activities` VALUES ('ce0d6c35-88da-4492-9669-ff68e828f309', '1987755992376414210', 'UPDATE_STATUS', 'find', '70620bf2-de33-4b47-bbd4-efe296eeb927', '招领物品:111已认领', '2026-01-28 10:43:44');
INSERT INTO `activities` VALUES ('d33755d6-fb49-4c0f-bc94-95f092a2a214', '1979079350726078466', 'UPDATE_STATUS', 'lost', '66acbc84-9ad6-40c4-92e3-d26a162a21e6', '失物杯子已找回', '2025-11-11 13:55:56');
INSERT INTO `activities` VALUES ('d3f80ae2-2676-438e-9368-f696946bd250', '1979079350726078466', 'UPDATE_STATUS', 'lost', '1ff5bb8f-68eb-446f-9c60-57f1b1db228e', '失物啊实打实大已找回', '2025-11-11 13:56:48');
INSERT INTO `activities` VALUES ('e377a2a5-79b8-4cbe-a011-a2a58cbe5609', '1982720631266926594', 'UPDATE_STATUS', 'find', '76d69bc9-fb58-4dca-a5fa-bfeda3021d3b', '招领物品:实打实已找回', '2025-11-05 15:23:01');
INSERT INTO `activities` VALUES ('e5e2a20e-5d0d-4203-ae12-875623b7e987', '1987755992376414210', 'PUBLISH', 'lost', 'e7c92d53-0525-48eb-bb41-6867efcb44e6', '发布了失物信息：111111', '2026-01-28 10:10:01');
INSERT INTO `activities` VALUES ('e8f2b317-bca6-476c-9031-7134cb5afb8f', '1979079350726078466', 'UPDATE_STATUS', 'lost', '015b0a13-e664-4c09-9c54-5a31eded8e84', '失物校服已找回', '2026-02-03 17:29:22');
INSERT INTO `activities` VALUES ('e9ea5fba-152d-4077-a479-d5929d8c1ff4', '1979079350726078466', 'PUBLISH', 'find', '6a63bdb3-97d4-4a27-96fd-5e3ace4d1e22', '发布了招领信息：背包', '2025-11-12 15:54:07');
INSERT INTO `activities` VALUES ('eaad5c62-c854-41c5-9543-0d3c27bbf03a', '1979079350726078466', 'PUBLISH', 'lost', 'd7e5be39-140c-4cd8-8ad0-4eff307523c3', '发布了失物信息：啊实打实大', '2025-11-12 10:53:05');
INSERT INTO `activities` VALUES ('f3189340-2f61-4a44-9109-79ff57a1e2f6', '1987755992376414210', 'PUBLISH', 'find', '46271170-7045-4d80-ad4e-c7e485628622', '发布了招领信息：1111111111111', '2026-01-28 10:51:04');
INSERT INTO `activities` VALUES ('f9e90d5c-4211-4205-b133-ef8b11172344', '1987755992376414210', 'PUBLISH', 'lost', '6ebfae5c-e30b-4fa6-8a0c-4a2eb5e04436', '发布了失物信息：111111', '2026-01-28 10:08:30');
INSERT INTO `activities` VALUES ('fd110289-3aba-437d-8344-da14803da43f', '1979079350726078466', 'UPDATE_STATUS', 'lost', '9b846012-2f27-4209-9ef3-6f85485a64c8', '失物啊实打实大已找回', '2025-11-12 15:38:23');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论ID',
  `info_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联信息ID',
  `info_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '信息类型（lost/find）',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `like_count` int NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '匿名用户' COMMENT '用户姓名',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像url',
  PRIMARY KEY (`id` DESC) USING BTREE,
  INDEX `idx_info`(`info_id` ASC, `info_type` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_cominfo`(`info_id` ASC, `info_type` ASC) USING BTREE,
  INDEX `idx_comuser_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_comcreate_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES ('fe6ce242-e285-4049-9a5d-6b222fd9f287', '7', 'lost', '1987755992376414210', '你好', 1, '2025-11-10 13:46:50', 'admin', NULL);
INSERT INTO `comment` VALUES ('fa818286-34c2-4042-b4bd-85beef8e70ed', '868612cb-f3e1-4bd0-9004-df9341f2e6cb', 'find', '1987755992376414210', '啊实打实大师大', 0, '2025-11-11 14:00:44', '管理员', NULL);
INSERT INTO `comment` VALUES ('e8dd47bc-13ed-447d-965b-d87851a306ef', 'f0bd336a-47cf-4489-b729-4cddc6295d7f', 'find', '2018165850192605186', '111', 0, '2026-02-02 11:36:26', 'max_12', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('e212d9cc-eee8-49f3-aab1-88f73bfffefc', '015b0a13-e664-4c09-9c54-5a31eded8e84', 'lost', '1979079350726078466', '@admin 你好', 0, '2025-12-11 20:00:26', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('dec05837-33b3-4600-977e-33224c497493', 'd7e5be39-140c-4cd8-8ad0-4eff307523c3', 'lost', '1987755992376414210', '你iii', 0, '2025-11-12 13:40:05', 'admin', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('d91c5830-9bf6-4cef-a0ae-e8c09b1d5dd3', 'a8ec2e09-b9fb-4e28-8c89-4476eadabe6e', 'lost', '1987755992376414210', '22222', 0, '2026-01-22 17:18:14', '管理员', NULL);
INSERT INTO `comment` VALUES ('d569158c-d1f3-43e5-ac6a-20ba5e3f0601', 'd7e5be39-140c-4cd8-8ad0-4eff307523c3', 'lost', '1987755992376414210', '你好', 0, '2025-11-12 13:39:57', 'admin', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('d531ed47-2a57-4a12-a01e-5e43b8a0b20d', 'f0bd336a-47cf-4489-b729-4cddc6295d7f', 'find', '2018165850192605186', '111111111111', 0, '2026-02-02 11:40:22', 'max_12', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('ccdd78d6-c46c-4eeb-bc32-9a4a9d1fcb5f', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '啊实打实大', 0, '2025-11-02 19:21:33', 'abcd', NULL);
INSERT INTO `comment` VALUES ('c7d7f732-d0b0-4de5-ac3d-b3a48107a279', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '阿斯顿发发生', 0, '2025-11-02 19:34:31', 'abcd', NULL);
INSERT INTO `comment` VALUES ('c5a98a76-b35c-4b64-8795-70f44c154bc9', 'f0a97937-de6b-40ee-b67c-f9dd7111d8b9', 'lost', '1979079350726078466', '你说的对，但是我没看到', 0, '2025-11-12 16:09:23', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('bd0a87b2-6b2d-46d3-86f9-1d1463ff8d58', '23e1a6bf-a1ff-4028-8409-4dbb867b1fa0', 'lost', '1987755992376414210', '不能是动物', 0, '2025-11-11 13:34:42', 'admin', NULL);
INSERT INTO `comment` VALUES ('a8cc0c43-7661-4d56-9583-bb0f0b418030', 'd7e5be39-140c-4cd8-8ad0-4eff307523c3', 'lost', '1987755992376414210', '@admin 水电费水电费', 0, '2025-11-12 13:40:12', 'admin', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('a7cf0df8-495e-4d37-b2b0-942b39faf411', 'd63d5ed2-71a6-4127-a84c-04d2ebd11419', 'find', '1979079350726078466', '阿奇撒大声地', 0, '2025-11-02 18:55:35', 'abcd', NULL);
INSERT INTO `comment` VALUES ('a0aa306b-988f-4513-a04b-238cfbca0f5d', '015b0a13-e664-4c09-9c54-5a31eded8e84', 'lost', '1987755992376414210', '你好', 2, '2025-12-11 19:58:24', 'admin', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('973b1503-9c61-4ccc-9fcd-2a9f2998b8de', '015b0a13-e664-4c09-9c54-5a31eded8e84', 'lost', '1979079350726078466', 'niaho', 2, '2026-01-21 14:58:31', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/ff1e13b1-f73d-4afd-bf2d-ed049dd3b3d0.png');
INSERT INTO `comment` VALUES ('93b2b1d8-42cb-4fd2-bb63-39268c5170f9', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '你好', 0, '2025-11-02 19:42:06', 'abcd', NULL);
INSERT INTO `comment` VALUES ('8f7849bd-cdf4-4b7d-9fad-41a565c2e74c', 'f0bd336a-47cf-4489-b729-4cddc6295d7f', 'find', '2018165850192605186', '@max_12 nishoudedui', 0, '2026-02-02 11:40:08', 'max_12', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('8a4d8330-777e-4803-a325-fc602eb739f6', '6ebfae5c-e30b-4fa6-8a0c-4a2eb5e04436', 'lost', '1987755992376414210', '111', 0, '2026-01-31 16:47:17', 'admin', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('898b68fc-de93-41fc-9089-4de6a45691cb', 'd589936c-1df3-4fdd-8524-97a00ee8dc5a', 'find', '1987755992376414210', '12222', 0, '2026-01-22 17:18:09', '管理员', NULL);
INSERT INTO `comment` VALUES ('894197b6-0126-499e-b1a7-8e8197bc48a7', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '阿萨德是的撒', 0, '2025-11-02 19:20:49', 'abcd', NULL);
INSERT INTO `comment` VALUES ('88ca00a5-6110-47c4-bd17-5876720b78e7', '46271170-7045-4d80-ad4e-c7e485628622', 'find', '1987755992376414210', '1111111111111', 0, '2026-01-28 10:51:32', 'admin', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('81d08eac-f378-4cdb-bbcd-51a5d1c613d1', '9', 'lost', '1979079350726078466', '啊实打实大', 0, '2025-11-02 19:25:33', 'abcd', NULL);
INSERT INTO `comment` VALUES ('7cfcf653-783b-4540-b728-fd38b3e0f843', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '湿哒哒', 1, '2025-11-03 14:44:42', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('7c74f5b6-2b8c-42c0-8179-e12341fa92f7', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '速度啊地方', 0, '2025-11-02 19:21:18', 'abcd', NULL);
INSERT INTO `comment` VALUES ('7859bc2e-00c1-4e71-9dd4-6a3205120fd6', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '啊实打实大', 1, '2025-11-03 14:40:59', '用户1', NULL);
INSERT INTO `comment` VALUES ('78058fb4-b526-4f62-94c6-814b1bfdf47f', 'b5f320c0-c613-4e0e-95c3-2d56b440a52d', 'lost', '1979079350726078466', '代发费', 1, '2025-11-03 14:56:40', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('73bc7661-bb70-4c0d-a309-4c8564fc4709', '015b0a13-e664-4c09-9c54-5a31eded8e84', 'lost', '1987755992376414210', '你号', 0, '2026-01-16 13:31:33', 'admin', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('7084f000-f235-4224-ac00-2602f0cadbe0', 'd63d5ed2-71a6-4127-a84c-04d2ebd11419', 'find', '1979079350726078466', '啊实打实大', 0, '2025-11-02 19:07:06', 'abcd', NULL);
INSERT INTO `comment` VALUES ('5eb53a35-c482-46d0-872b-c356f5eca8dd', '43381b07-e800-4cbb-ac4d-5bb0dcd6a10b', 'lost', '1982720631266926594', '胜多负少', 0, '2025-11-05 15:28:45', 'abcde', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/4f0bd1bd-3aab-4bdd-94da-86ade64f8054.png');
INSERT INTO `comment` VALUES ('5d4589b5-6a98-4239-8892-45cd3ddc357a', '1ff5bb8f-68eb-446f-9c60-57f1b1db228e', 'lost', '1979079350726078466', '好', 0, '2025-11-11 13:56:53', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('59520ef7-e12a-4183-8260-e7fd5cfca992', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '啊实打实大', 0, '2025-11-02 19:12:11', 'abcd', NULL);
INSERT INTO `comment` VALUES ('54525c3c-7772-4051-bc42-2b37399b9d6e', 'ca1fddcc-ce7c-4a89-b4dd-6407de3bf4af', 'find', '1987755992376414210', '啊时代大厦大多数', 0, '2025-11-12 10:53:14', '管理员', NULL);
INSERT INTO `comment` VALUES ('53ab2d5c-76af-4c9f-9d9a-abf71fa4369a', 'c3401f3b-7ae5-42eb-8c50-e187409e518c', 'lost', '1987755992376414210', '22222222', 0, '2026-01-22 17:18:17', '管理员', NULL);
INSERT INTO `comment` VALUES ('52364023-7eae-4a10-98fb-63e54d60ab91', 'f0bd336a-47cf-4489-b729-4cddc6295d7f', 'find', '2018165850192605186', '11111111111', 0, '2026-02-02 11:40:32', 'max_12', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('502e7d9a-4343-4797-9632-a7d4a20af512', 'f440d7e7-5456-41b3-9b3d-bfcc8ad96e88', 'lost', '1987755992376414210', '啊实打实大撒大声地', 0, '2025-11-11 14:00:48', '管理员', NULL);
INSERT INTO `comment` VALUES ('33456801-7b58-4e4f-b00c-67f9aa604f8d', 'f0bd336a-47cf-4489-b729-4cddc6295d7f', 'find', '2018165850192605186', '11111111111111111111111111111111111111111111111', 1, '2026-02-02 11:39:54', 'max_12', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('2f796ff4-823b-4d2d-8615-6e08dd76e919', 'f0bd336a-47cf-4489-b729-4cddc6295d7f', 'find', '2018165850192605186', '11111111', 0, '2026-02-02 11:40:17', 'max_12', '/src/assets/images/1.png');
INSERT INTO `comment` VALUES ('29ef7246-b262-4606-aefb-a0669ff5054d', '5214310f-c908-470f-a06d-6d6468a75343', 'find', '1979079350726078466', '11111111111111111111111111111111111111111111', 0, '2026-02-03 17:47:45', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8926e237-1add-46f1-a662-43cdfa007951.png');
INSERT INTO `comment` VALUES ('29cbb027-fc41-4769-8c2b-06d5dc9985f1', '5214310f-c908-470f-a06d-6d6468a75343', 'find', '1979079350726078466', '1111111111111', 0, '2026-02-03 17:47:42', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8926e237-1add-46f1-a662-43cdfa007951.png');
INSERT INTO `comment` VALUES ('25a72411-0a46-46a5-9579-8a8283328d96', '8', 'lost', '1979079350726078466', '奥术大师', 1, '2025-11-05 15:58:08', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('1cf80290-11d0-4488-9689-043f4c31cd4c', 'f0a97937-de6b-40ee-b67c-f9dd7111d8b9', 'lost', '1982720631266926594', '我没看到，但是我替你着急', 1, '2025-11-12 16:10:20', 'abcde', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/4f0bd1bd-3aab-4bdd-94da-86ade64f8054.png');
INSERT INTO `comment` VALUES ('1629d34a-b722-4dee-8602-4deb5aa9af7b', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '阿萨法守法', 0, '2025-11-02 19:21:23', 'abcd', NULL);
INSERT INTO `comment` VALUES ('13c7d920-9a40-493d-8903-8d31ba031378', 'b9441a28-f32d-4325-96d4-c6879cbe3d6d', 'lost', '1979079350726078466', '你好', 1, '2025-11-03 13:54:46', 'abcd', NULL);
INSERT INTO `comment` VALUES ('0d731e4b-1445-4c77-8508-c5ff1cc35a4e', '8', 'lost', '1979079350726078466', '知识点都知道', 1, '2025-11-05 15:58:29', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('0c62cf68-5cc4-40d7-a062-44756961e0f8', '5214310f-c908-470f-a06d-6d6468a75343', 'find', '1979079350726078466', '11111111', 0, '2026-02-03 17:47:40', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8926e237-1add-46f1-a662-43cdfa007951.png');
INSERT INTO `comment` VALUES ('078f5c61-fadd-4689-b9dc-7e4e67d5dc9e', 'b65b537d-e10b-477b-9d5c-7163d6c55431', 'lost', '1987755992376414210', '啊实打实大大', 0, '2025-11-11 13:54:10', '管理员', NULL);
INSERT INTO `comment` VALUES ('05430b5e-d989-426d-97bd-b39df5f5fa35', '8', 'lost', '1982720631266926594', '阿萨大大打算', 1, '2025-11-03 15:00:32', 'abcde', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/4f0bd1bd-3aab-4bdd-94da-86ade64f8054.png');
INSERT INTO `comment` VALUES ('0412df6d-7145-4cb3-bf2f-5c27a501e9fe', 'b5f320c0-c613-4e0e-95c3-2d56b440a52d', 'lost', '1979079350726078466', '阿斯达时代大厦', 0, '2025-11-03 15:51:11', 'abcd', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7c9e6b8-6e2d-4cee-b5ce-1be27b499950.png');
INSERT INTO `comment` VALUES ('00595e73-72da-42ce-94b3-3e56ed9b2e20', '0c2213d2-a5d7-485e-afad-bf5cc53b63b7', 'lost', '1987755992376414210', '不合法发的丰富的', 0, '2026-01-17 19:39:20', '管理员', NULL);

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点赞记录ID',
  `comment_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_comment_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment_like
-- ----------------------------
INSERT INTO `comment_like` VALUES ('2987e653-f823-4b67-9f35-9540e2d213ca', '13c7d920-9a40-493d-8903-8d31ba031378', '1982720631266926594', '2025-11-05 16:00:38');
INSERT INTO `comment_like` VALUES ('6568d4aa-9d01-4b24-ba1c-165c03f98e37', '0d731e4b-1445-4c77-8508-c5ff1cc35a4e', '1987755992376414210', '2025-11-10 13:55:57');
INSERT INTO `comment_like` VALUES ('65d16f20-6254-4225-9049-e26ca26568ab', '25a72411-0a46-46a5-9579-8a8283328d96', '1987755992376414210', '2025-11-10 13:55:56');
INSERT INTO `comment_like` VALUES ('81ad5d06-be28-43f9-a2c8-4ebad2ac1c29', '78058fb4-b526-4f62-94c6-814b1bfdf47f', '1979079350726078466', '2025-11-03 14:56:43');
INSERT INTO `comment_like` VALUES ('913d620b-0204-4e20-b747-0184f4e814b9', '7859bc2e-00c1-4e71-9dd4-6a3205120fd6', '1982720631266926594', '2025-11-05 16:00:37');
INSERT INTO `comment_like` VALUES ('92d9162a-e24c-4742-9abd-f4f3a7e1b439', '05430b5e-d989-426d-97bd-b39df5f5fa35', '1982720631266926594', '2025-11-03 15:00:34');
INSERT INTO `comment_like` VALUES ('9ccaad31-305a-4b24-be2e-df74451241b3', 'a0aa306b-988f-4513-a04b-238cfbca0f5d', '1979079350726078466', '2025-12-11 20:00:17');
INSERT INTO `comment_like` VALUES ('a3226d42-da55-4640-aab3-160d1a080cf0', '33456801-7b58-4e4f-b00c-67f9aa604f8d', '2018165850192605186', '2026-02-02 11:40:00');
INSERT INTO `comment_like` VALUES ('a899a6cf-d90c-440d-896e-15e86e3877bf', 'fe6ce242-e285-4049-9a5d-6b222fd9f287', '1987755992376414210', '2025-11-10 13:46:55');
INSERT INTO `comment_like` VALUES ('c00a2cf5-bcd8-449f-8437-8c7d141241cf', 'a0aa306b-988f-4513-a04b-238cfbca0f5d', '1987755992376414210', '2025-12-11 19:58:26');
INSERT INTO `comment_like` VALUES ('c5b5f53d-91f3-4e77-9901-97e13e334afa', '7cfcf653-783b-4540-b728-fd38b3e0f843', '1979079350726078466', '2025-11-03 15:08:41');
INSERT INTO `comment_like` VALUES ('dba23d98-95b8-4c9b-b688-6bf9754e4706', '1cf80290-11d0-4488-9689-043f4c31cd4c', '1982720631266926594', '2025-11-12 16:10:38');
INSERT INTO `comment_like` VALUES ('f8c1ab09-6695-48db-a1c2-6de4549efc7d', '973b1503-9c61-4ccc-9fcd-2a9f2998b8de', '2018165850192605186', '2026-02-02 11:59:13');
INSERT INTO `comment_like` VALUES ('fa0d18b9-e0cc-45e5-8c4f-b382902e07c8', '973b1503-9c61-4ccc-9fcd-2a9f2998b8de', '1987755992376414210', '2026-01-31 16:16:33');

-- ----------------------------
-- Table structure for find_info
-- ----------------------------
DROP TABLE IF EXISTS `find_info`;
CREATE TABLE `find_info`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '信息ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发布用户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品类型',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发现地点',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '物品描述',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL，JSON数组格式',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING/APPROVED/REJECTED/SOLVED）',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `found_time` datetime NULL DEFAULT NULL COMMENT '捡到时间',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_status_publish_time`(`status` ASC, `publish_time` ASC) USING BTREE,
  INDEX `idx_status_view`(`status` ASC, `view_count` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  CONSTRAINT `fk_find_info_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '招领信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of find_info
-- ----------------------------
INSERT INTO `find_info` VALUES ('0d280d4a-4ed6-435b-a077-9c281266638f', '1987755992376414210', '111', 'ELECTRONICS', '111', '11111111', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'APPROVED', NULL, 10, '2025-11-10 13:38:30', '2026-02-03 16:19:58', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('1bab60b9-90dc-46fb-9ca1-38fbf86337c5', '1982720631266926594', '智能手机', 'ELECTRONICS', '在天台边上', '看起来完好，确不能开机', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/ea26c984-da21-4ccc-a1fd-0c9e7a9a425f.jpg\"]', '16822565778', 'woshixiaoxuesheng@qq.com', 'APPROVED', NULL, 19, '2025-11-12 15:57:35', '2026-02-03 17:15:04', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('1bf9e57b-2b4f-4f34-b042-519a3975e111', '1979079350726078466', '阿萨德', 'ID_CARD', '萨达撒大声地', '啊实打实大撒大声地', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/188566d7-2723-4ee4-83bc-8c04d8eda9e4.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'APPROVED', NULL, 7, '2025-11-09 16:08:11', '2026-01-22 17:13:35', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('2718a9cf-4c3a-4558-a980-3e0c5dbcfa93', '1979079350726078466', '哈基米', 'OTHER', '非洲', '白色蹦跳小猫', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/679b7705-8ef4-4e96-bea0-26945c1ee1fe.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 7, '2025-11-09 14:58:22', '2025-11-12 15:39:03', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('42686947-e680-469e-8dbe-5aa4959e1ad6', '2018165850192605186', '粉色袜子', 'CLOTHES', '凹凹', '啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/4d9ca01f-2bad-49bd-8bbc-014dc859c057.jpg\"]', '13556788876', '12312399@nb.com', 'SOLVED', NULL, 0, '2026-02-02 13:55:17', '2026-02-02 13:55:17', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('46271170-7045-4d80-ad4e-c7e485628622', '1987755992376414210', '1111111111111', 'ELECTRONICS', '22222222222', '22222222222222222', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/9c99240b-9e4f-4f9c-af86-dbb295662288.png\"]', '18458627655', '2497023788@qq.com', 'SOLVED', NULL, 13, '2026-01-28 10:50:38', '2026-02-02 11:22:57', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('5214310f-c908-470f-a06d-6d6468a75343', '1979079350726078466', '111', 'ID_CARD', '111111111111', '111111111111111', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b7659b3c-17f2-4089-bb4a-55b0339974ef.jpg\"]', '18458627655', '2497023788@qq.com', 'APPROVED', NULL, 3, '2026-02-02 16:05:37', '2026-02-03 17:47:35', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('5628a7c2-b5a2-4e45-8745-fb996f08ab32', '1979079350726078466', '手机', 'ELECTRONICS', '南三教学楼', '白色手机巴掌大小', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/1d97d630-3a80-4d8f-b397-d1f32ed47abb.jpg\"]', '18458627655', '2497023788@qq.com', 'SOLVED', NULL, 10, '2026-01-16 09:28:44', '2026-01-31 15:58:21', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('5b286573-1362-484e-a649-bda032985a88', '1982720631266926594', '啊实打实大大', 'OTHER', '阿瓦达我单位大大大', '阿瓦达我打我打我的娃', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/daa4e1bc-eeab-49ea-9126-34999532e02f.png\",\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b8ad02fa-4601-4d6b-a436-0e328809a4da.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 13, '2025-11-03 15:35:28', '2025-11-12 15:39:53', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('6a63bdb3-97d4-4a27-96fd-5e3ace4d1e22', '1979079350726078466', '背包', 'DAILY_NECESSITIES', '西教学楼5楼', '大号旅行背包', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/bbca1ec6-d2ab-4e6f-8c5b-eb9d4c599ebc.jpg\"]', '16822565778', 'firminoluas@gmail.com', 'SOLVED', NULL, 5, '2025-11-12 15:53:22', '2026-01-27 17:17:42', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('70620bf2-de33-4b47-bbd4-efe296eeb927', '1987755992376414210', '111', 'ELECTRONICS', '2222', '11111111111111', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/50c44f46-6203-42eb-915d-d87610c1e552.png\"]', '18458627655', '2497023788@qq.com', 'SOLVED', NULL, 2, '2026-01-28 10:43:09', '2026-01-28 10:43:43', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('76d69bc9-fb58-4dca-a5fa-bfeda3021d3b', '1982720631266926594', '实打实', 'BOOKS', '阿萨大大十点多', '阿斯达大厦', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 2, '2025-11-05 15:01:33', '2025-11-07 17:59:26', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('7ac6a6cd-a7b4-48b8-8a25-8af6517b0266', '1979079350726078466', '雨衣', 'CLOTHES', '男宿', '褐色带血迹，雨衣', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/786bf4b9-e3b5-4475-9bb4-209c067d91d0.jpg\"]', '16822565778', 'firminoluas@gmail.com', 'SOLVED', NULL, 0, '2026-02-02 13:54:21', '2026-02-02 13:54:21', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('868612cb-f3e1-4bd0-9004-df9341f2e6cb', '1987755992376414210', '啊实打实大大是的', 'ELECTRONICS', '啊实打实大师', '啊实打实大撒大声地', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'APPROVED', NULL, 3, '2025-11-11 14:00:37', '2026-02-03 14:40:06', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('8f6bd347-7714-4e8b-b4cc-3c084e73dca0', '1979079350726078466', '太阳眼镜', 'DAILY_NECESSITIES', '在食堂二楼', '黑色的太阳墨镜', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/d1acd016-3b0b-4eed-9037-5cae3e019e22.jpg\"]', '18267250465', '2eeea@168.com', 'SOLVED', NULL, 3, '2025-11-12 15:49:29', '2026-01-31 15:38:32', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('ca018f5d-1d03-4209-915c-b22faf7ea6be', '2018250806839341057', '1111', 'ELECTRONICS', '11111111111111', '11111111111111111111111111111111', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/53691cb1-c09e-4943-9790-cb533581644f.jpg\"]', '18458627655', '2497023788@qq.com', 'APPROVED', NULL, 2, '2026-02-02 17:13:33', '2026-02-03 17:19:55', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('ca1fddcc-ce7c-4a89-b4dd-6407de3bf4af', '1987755992376414210', '雨伞', 'DAILY_NECESSITIES', '南区操场', '蓝色雨伞，收缩式', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/eb5aa879-2126-4b53-a79c-644b09ca9c56.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'APPROVED', NULL, 3, '2025-11-12 10:39:55', '2026-01-22 17:13:31', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('d589936c-1df3-4fdd-8524-97a00ee8dc5a', '1979079350726078466', '111111', 'ID_CARD', '11111', '前五大撒大声地', '[]', '112333333', NULL, 'REJECTED', NULL, 3, '2026-01-22 17:12:20', '2026-01-22 17:17:06', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('d63d5ed2-71a6-4127-a84c-04d2ebd11419', '1979079350726078466', '啊实打实大', 'ID_CARD', '阿斯达大厦', '啊实打实大删掉', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/f059a093-6e28-425a-a8d4-a3d5b829fb10.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 38, '2025-11-02 16:27:25', '2025-11-12 15:38:47', NULL, NULL, 0);
INSERT INTO `find_info` VALUES ('f0bd336a-47cf-4489-b729-4cddc6295d7f', '1987755992376414210', '1111111', 'BOOKS', '1111111', '111111111', '[]', '18458627655', '2497023788@qq.com', 'SOLVED', NULL, 17, '2026-01-31 16:48:57', '2026-02-02 11:51:23', NULL, NULL, 0);

-- ----------------------------
-- Table structure for honor_period
-- ----------------------------
DROP TABLE IF EXISTS `honor_period`;
CREATE TABLE `honor_period`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '光荣榜周期ID，对应 periodId',
  `period_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'WEEKLY' COMMENT '周期类型：WEEKLY 等',
  `period_start` datetime(3) NOT NULL COMMENT '周期开始时间',
  `period_end` datetime(3) NOT NULL COMMENT '周期结束时间',
  `top_n` int NOT NULL DEFAULT 10 COMMENT '榜单前 N 名',
  `total_completed_count` int NOT NULL DEFAULT 0 COMMENT '本周期完成总次数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NOT_SENT' COMMENT 'NOT_SENT / SENT / AWARDED',
  `send_time` datetime(3) NULL DEFAULT NULL COMMENT '标记已发送学校时间',
  `award_time` datetime(3) NULL DEFAULT NULL COMMENT '标记已完成颁奖时间',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_honor_period_type_status_start`(`period_type` ASC, `status` ASC, `period_start` ASC) USING BTREE,
  INDEX `idx_honor_period_start_end`(`period_start` ASC, `period_end` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '光荣榜周期表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of honor_period
-- ----------------------------
INSERT INTO `honor_period` VALUES ('3aa52c3bee5c44f79c46b2072ea81428', 'WEEKLY', '2026-01-26 00:00:00.000', '2026-01-29 00:00:00.000', 10, 1, 'NOT_SENT', NULL, NULL, '2026-01-28 11:33:36.537', '2026-01-28 11:33:36.537');
INSERT INTO `honor_period` VALUES ('59c75ec48a38421a94ce70004f1ec47a', 'WEEKLY', '2025-12-01 00:00:00.000', '2026-01-02 00:00:00.000', 10, 0, 'NOT_SENT', NULL, NULL, '2026-02-02 11:21:29.028', '2026-02-02 11:21:29.028');
INSERT INTO `honor_period` VALUES ('c7d80cf1df69495eb77629c6742d6585', 'WEEKLY', '2026-01-19 00:00:00.000', '2026-01-26 00:00:00.000', 10, 0, 'NOT_SENT', NULL, NULL, '2026-02-02 13:57:02.980', '2026-02-02 13:57:02.980');
INSERT INTO `honor_period` VALUES ('e61617f907b04861bc1b1c494401e8a6', 'WEEKLY', '2026-01-19 00:00:00.000', '2026-01-19 00:00:00.000', 10, 0, 'NOT_SENT', NULL, NULL, '2026-01-28 11:07:35.220', '2026-01-28 11:07:35.220');
INSERT INTO `honor_period` VALUES ('febffa076c8c4d0e9ed90540562b8b31', 'WEEKLY', '2026-02-02 00:00:00.000', '2026-02-03 00:00:00.000', 10, 3, 'AWARDED', '2026-02-03 17:27:52.883', '2026-02-03 17:28:02.033', '2026-02-02 13:57:08.238', '2026-02-02 13:57:08.238');

-- ----------------------------
-- Table structure for honor_period_item
-- ----------------------------
DROP TABLE IF EXISTS `honor_period_item`;
CREATE TABLE `honor_period_item`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主键ID',
  `period_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '光荣榜周期ID，关联 honor_period.id',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rank` int NOT NULL COMMENT '名次（1,2,3, ...）',
  `completed_count` int NOT NULL DEFAULT 0 COMMENT '本周期完成次数',
  `points` int NOT NULL DEFAULT 0 COMMENT '积分',
  `last_completed_at` datetime(3) NULL DEFAULT NULL COMMENT '最后一次完成时间',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名（冗余）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名（冗余）',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '班级名称（冗余）',
  `department_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学院名称（冗余）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL（冗余）',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_honor_item_period_rank`(`period_id` ASC, `rank` ASC) USING BTREE,
  INDEX `idx_honor_item_period_user`(`period_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `fk_honor_item_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_honor_item_period` FOREIGN KEY (`period_id`) REFERENCES `honor_period` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_honor_item_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '光荣榜周期明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of honor_period_item
-- ----------------------------
INSERT INTO `honor_period_item` VALUES ('3d62cc6c9fdb4b1c97cf8b32f8fa0a18', 'febffa076c8c4d0e9ed90540562b8b31', '1987755992376414210', 1, 2, 20, '2026-02-02 11:51:23.000', 'admin', 'max', NULL, NULL, 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/ae9eccb2-8eda-41ce-8319-9cf7c656c799.png', '2026-02-02 13:57:08.241', '2026-02-02 13:57:08.241');
INSERT INTO `honor_period_item` VALUES ('573b6d688b5c4709b80a59a37da021c3', 'febffa076c8c4d0e9ed90540562b8b31', '2018165850192605186', 3, 1, 10, '2026-02-02 13:55:17.000', 'max_12', '小红', NULL, NULL, '/src/assets/images/1.png', '2026-02-02 13:57:08.250', '2026-02-02 13:57:08.250');
INSERT INTO `honor_period_item` VALUES ('64f54fd40a5642d4a382a3bcdfcfc8b8', 'febffa076c8c4d0e9ed90540562b8b31', '1979079350726078466', 2, 1, 10, '2026-02-02 13:54:21.000', 'abcd', '李双', NULL, NULL, 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8926e237-1add-46f1-a662-43cdfa007951.png', '2026-02-02 13:57:08.247', '2026-02-02 13:57:08.247');
INSERT INTO `honor_period_item` VALUES ('e2a572168482499c9265b08c9c3f7de6', '3aa52c3bee5c44f79c46b2072ea81428', '1987755992376414210', 1, 1, 10, '2026-01-28 10:43:43.000', 'admin', 'max', NULL, NULL, '/src/assets/images/1.png', '2026-01-28 11:33:36.566', '2026-01-28 11:33:36.566');

-- ----------------------------
-- Table structure for lost_info
-- ----------------------------
DROP TABLE IF EXISTS `lost_info`;
CREATE TABLE `lost_info`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '信息ID',
  `user_id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发布用户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品类型',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '丢失地点',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '物品描述',
  `images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片URL，JSON数组格式',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '状态（PENDING/APPROVED/REJECTED/SOLVED）',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `lost_time` datetime NULL DEFAULT NULL COMMENT '丢失时间',
  `contact_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_publish_time`(`publish_time` ASC) USING BTREE,
  INDEX `idx_status_publish_time`(`status` ASC, `publish_time` ASC) USING BTREE,
  INDEX `idx_status_view`(`status` ASC, `view_count` ASC) USING BTREE,
  CONSTRAINT `fk_lost_info_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '失物信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of lost_info
-- ----------------------------
INSERT INTO `lost_info` VALUES ('015b0a13-e664-4c09-9c54-5a31eded8e84', '1979079350726078466', '校服', 'CLOTHES', '西区教学楼', '蓝色校服，经典宽', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/348c7622-df46-495e-9beb-a1d884f2b137.jpg\",\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/22844a63-9baf-475f-b50e-636960168275.png\"]', '15888885858', 'tangmeng@tt.com', 'SOLVED', NULL, 45, '2025-11-19 16:14:57', '2026-02-03 17:29:22', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('01998f14-3977-4be9-957a-7df729477362', '1979079350726078466', '阿达阿达阿达', 'ID_CARD', '啊实打实大师大', '啊实打实大师大', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/b3874642-7156-4e32-8f09-65b63667b3a3.png\",\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8ed207fb-ef07-49b7-9458-e65478c97d74.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 10, '2025-11-01 17:16:02', '2026-01-21 15:34:50', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('0c2213d2-a5d7-485e-afad-bf5cc53b63b7', '1979079350726078466', '咋水电费', 'ELECTRONICS', '啊大叔大婶', '阿达撒大声地', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/3b8b4e2e-c195-4d2a-abea-567cebb3947f.jpg\"]', '18458627655', '2497023788@qq.com', 'REJECTED', NULL, 2, '2026-01-17 19:35:43', '2026-01-21 15:35:07', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('0e39a4fa-ec9a-45b8-87f8-277aba1581d6', '1987755992376414210', '11111', 'ELECTRONICS', '11111', '111111111', '[]', '18458627655', '2497023788@qq.com', 'APPROVED', NULL, 7, '2026-01-31 16:48:37', '2026-02-03 17:31:08', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('1d361a31-1bc4-4645-a404-54003e517888', '1979079350726078466', '1111111', 'ID_CARD', '222222', '请问实打实大苏打', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/df7a14d6-f385-40e5-a339-df9ba122f7e2.jpg\",\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/0062556d-f283-4c4a-8ed4-8e4862efbc9c.jpg\"]', '18358262541', NULL, 'SOLVED', NULL, 2, '2026-01-22 17:22:29', '2026-01-22 17:22:48', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('1ff5bb8f-68eb-446f-9c60-57f1b1db228e', '1979079350726078466', '啊实打实大', 'ID_CARD', '啊实打实大', '阿斯达大厦', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 2, '2025-11-01 17:01:55', '2025-11-11 13:56:48', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('23e1a6bf-a1ff-4028-8409-4dbb867b1fa0', '1987755992376414210', '小猫', 'OTHER', '男宿', '1阿萨法方式方法', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/f884e44f-b853-417f-a472-a14894dec42c.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'REJECTED', NULL, 2, '2025-11-11 13:33:29', '2025-11-11 13:34:32', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('37c02eb5-13bd-460d-9e46-b24fc6a30e7a', '1982720631266926594', '钱包', 'ELECTRONICS', '啊啊啊啊啊啊啊', '啊啊啊啊啊啊啊啊啊啊啊啊啊啊啊', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'APPROVED', NULL, 22, '2025-11-05 15:17:28', '2026-02-02 16:12:24', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('43381b07-e800-4cbb-ac4d-5bb0dcd6a10b', '1982720631266926594', '小米手机', 'ELECTRONICS', '大润发', '黑的，白的红豆', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/37609fcb-fc5f-4663-99bf-9a5924e58282.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'REJECTED', NULL, 7, '2025-11-05 14:21:34', '2025-11-09 16:13:01', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('4abd0ba1-c5c0-4ed0-b43b-ecc7acf308d2', '1979079350726078466', '伞', 'DAILY_NECESSITIES', '在北区操场的草地上', '蓝色，收缩折叠伞', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8b303bd0-bd3d-4767-9f8a-d7df4342ea3e.png\"]', '18458627655', '249702378@qq.com', 'APPROVED', NULL, 8, '2025-11-12 15:47:50', '2026-02-03 16:40:47', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('4cfee812-c7b6-40ac-bd19-747d55620489', '1979079350726078466', '小米', 'ELECTRONICS', '撒旦法地方', 'sad发发胜多负少防守打法', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 6, '2025-11-09 16:07:26', '2025-11-12 15:25:28', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('64111d69-1676-4a9e-9de8-8ade9f6ac97e', '1979079350726078466', '11111111111', 'ID_CARD', '111111111111111', '111111111111111', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'APPROVED', NULL, 2, '2026-02-02 16:09:51', '2026-02-02 16:11:06', NULL, NULL, 1);
INSERT INTO `lost_info` VALUES ('6ebfae5c-e30b-4fa6-8a0c-4a2eb5e04436', '1987755992376414210', '111111', 'ELECTRONICS', '11111111', '11111111111111111111111', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'APPROVED', NULL, 4, '2026-01-28 09:22:26', '2026-01-31 16:47:12', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('9b846012-2f27-4209-9ef3-6f85485a64c8', '1979079350726078466', '啊实打实大', 'ELECTRONICS', '挖到第五大道', '瓦达我的爱我的', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 1, '2025-11-01 17:03:11', '2025-11-12 15:38:23', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('a8ec2e09-b9fb-4e28-8c89-4476eadabe6e', '1979079350726078466', 'x', 'ID_CARD', 'ASDASD', '12313123', '[]', '111111', NULL, 'SOLVED', NULL, 2, '2026-01-21 17:49:50', '2026-01-22 17:17:12', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('b65b537d-e10b-477b-9d5c-7163d6c55431', '1987755992376414210', '阿萨德', 'ID_CARD', '啊实打实大', '啊实打实大', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'REJECTED', NULL, 2, '2025-11-11 13:43:04', '2025-11-11 13:54:23', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('b9441a28-f32d-4325-96d4-c6879cbe3d6d', '1979079350726078466', 'test', 'CLOTHES', '啊士大夫大声道萨芬', '收到发的范德萨发的防守打法', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/1fbc99e3-eee4-4721-a312-a131a5bf9dcd.png\",\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/40362321-0523-43de-9c50-d54e034d4e83.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 97, '2025-11-02 18:49:34', '2025-11-12 15:37:16', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('c3401f3b-7ae5-42eb-8c50-e187409e518c', '1979079350726078466', '1231', 'ID_CARD', '1231312', '12请问大神', '[]', '12312313123', NULL, 'REJECTED', NULL, 1, '2026-01-21 17:45:14', '2026-01-22 17:18:18', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('d12fe219-3feb-4604-87c5-7e8e44b381a5', '1987755992376414210', '透明白色杯子', 'DAILY_NECESSITIES', '厕所', '反正就是普通杯子', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/cbd87e36-624d-4125-a365-9192cc40a809.jpg\"]', '15888885858', 'tangmeng@tt.com', 'SOLVED', NULL, 5, '2025-11-12 16:13:36', '2025-12-22 16:46:14', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('d7e5be39-140c-4cd8-8ad0-4eff307523c3', '1979079350726078466', '啊实打实大', 'ELECTRONICS', '啊实打实大师大', '啊实打实大时代撒大声地', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/66bf20b1-744a-449d-9e69-d8a1c7084312.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 14, '2025-11-12 10:51:59', '2025-11-12 15:38:07', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('e563bd0d-3e57-42f2-ad27-9eb7c0653136', '1982720631266926594', '啊实打实大师大', 'ID_CARD', '达瓦的大大的', '阿瓦达大大我打撒大声地', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/34d88310-77ad-4fb6-bd14-6f3929f26f03.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 7, '2025-11-03 15:01:12', '2025-12-18 19:55:10', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('e7c92d53-0525-48eb-bb41-6867efcb44e6', '1987755992376414210', '111111', 'ELECTRONICS', '22222', '111111111111111', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/0775b34b-877f-41ee-a76d-9723175e5e06.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 3, '2026-01-28 10:09:48', '2026-01-28 10:51:10', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('ef7bca95-736c-490f-926d-52e6f4aae6d0', '1979079350726078466', '11111111', 'ELECTRONICS', '222222222222', '2222222222', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/429340fd-b60b-4fb3-8e19-0f7e2fb35237.jpg\"]', '18458627655', '2497023788@qq.com', 'APPROVED', NULL, 2, '2026-02-02 16:11:47', '2026-02-04 11:23:34', NULL, NULL, 1);
INSERT INTO `lost_info` VALUES ('f0a97937-de6b-40ee-b67c-f9dd7111d8b9', '1982720631266926594', '衣服', 'CLOTHES', '在舞蹈房', '红色松紧的宽大衣服', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/5641ac20-37c8-4b12-9936-417ffbae27fe.jpg\"]', '13556788876', '12312399@nb.com', 'APPROVED', NULL, 2, '2025-11-12 15:55:32', '2025-11-12 16:11:11', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('f440d7e7-5456-41b3-9b3d-bfcc8ad96e88', '1987755992376414210', '阿斯达大厦', 'ELECTRONICS', '啊实打实大多数', '啊实打实大', '[]', '18458627655', '2222510014@s.nbufe.edu.cn', 'REJECTED', NULL, 1, '2025-11-11 14:00:19', '2025-11-11 14:00:54', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('f8dc65b0-3007-4f52-8a2b-0678b5f0d621', '1982720631266926594', '阿斯达大厦', 'BOOKS', '啊实打实大撒大声地', '阿达大大撒', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/3a72ce84-3a84-4f09-ba5c-ff9ae6bb833d.png\",\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8031d1ff-629a-4406-931a-9bee248c331b.png\"]', '18458627655', '2222510014@s.nbufe.edu.cn', 'SOLVED', NULL, 6, '2025-11-03 15:17:28', '2025-11-12 15:39:33', NULL, NULL, 0);
INSERT INTO `lost_info` VALUES ('fcc3cf76-997c-4022-b9a6-1654519ab56c', '1979079350726078466', '保温杯', 'DAILY_NECESSITIES', '食堂3楼', '大号杯子，保温杯', '[\"https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/6d0799bc-5501-4896-a490-524191d751a6.jpg\"]', '17817899573', 'firminoluas@gmail.com', 'APPROVED', NULL, 5, '2025-11-12 15:51:07', '2026-02-02 11:09:05', NULL, NULL, 0);

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人',
  `action_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '详情',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2018890000472502275 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (2018889780665806849, 1987755992376414210, 'AUDIT', '{\"args\":{\"idAndType\":{\"id\":\"5214310f-c908-470f-a06d-6d6468a75343\",\"type\":\"find\",\"reason\":null}},\"method\":\"pass\",\"ip\":\"0:0:0:0:0:0:0:1\",\"resultCode\":0,\"operationType\":\"AUDIT\",\"resultMessage\":\"操作成功\",\"uri\":\"/api/audit/pass\",\"timestamp\":\"2026-02-04T11:30:07.511301900\"}', '2026-02-04 11:30:08');
INSERT INTO `operation_log` VALUES (2018889975860326402, 1987755992376414210, 'DELETE', '{\"args\":{\"id\":\"ef7bca95-736c-490f-926d-52e6f4aae6d0\"},\"method\":\"deleteLostInfo\",\"ip\":\"0:0:0:0:0:0:0:1\",\"resultCode\":0,\"operationType\":\"DELETE\",\"resultMessage\":\"操作成功\",\"uri\":\"/api/admin/lost/ef7bca95-736c-490f-926d-52e6f4aae6d0\",\"timestamp\":\"2026-02-04T11:30:54.066424700\"}', '2026-02-04 11:30:54');
INSERT INTO `operation_log` VALUES (2018890000472502274, 1987755992376414210, 'DELETE', '{\"args\":{\"id\":\"64111d69-1676-4a9e-9de8-8ade9f6ac97e\"},\"method\":\"deleteLostInfo\",\"ip\":\"0:0:0:0:0:0:0:1\",\"resultCode\":0,\"operationType\":\"DELETE\",\"resultMessage\":\"操作成功\",\"uri\":\"/api/admin/lost/64111d69-1676-4a9e-9de8-8ade9f6ac97e\",\"timestamp\":\"2026-02-04T11:30:59.932271600\"}', '2026-02-04 11:31:00');

-- ----------------------------
-- Table structure for student_whitelist
-- ----------------------------
DROP TABLE IF EXISTS `student_whitelist`;
CREATE TABLE `student_whitelist`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学号',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学院',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学生白名单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_whitelist
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` varchar(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '/src/assets/images/1.png' COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'STUDENT' COMMENT '角色（STUDENT/TEACHER/ADMIN）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `account_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账户名',
  `student_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学院',
  `status` tinyint NULL DEFAULT 0 COMMENT '状态 0:正常 1:封禁',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `account_name`(`account_name` ASC) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1978820643241050113', '1231', '123abc', 'asd', '17815966655', '123sdf@qq.com', '/src/assets/images/1.png', 'STUDENT', '2025-10-16 21:49:40', '2025-10-16 21:49:40', '1231', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('1978821044648525826', 'sring', 'string', 'string', 'string', 'string', '/src/assets/images/1.png', 'STUDENT', '2025-10-16 21:51:16', '2025-10-16 21:51:16', 'sring', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('1978823936008736769', '123444', 'abc123', '123', '17815366628', '123sdf@qq.com', '/src/assets/images/1.png', 'STUDENT', '2025-10-16 22:02:45', '2025-10-16 22:02:45', '123444', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('1978824488084000770', '123', '123zxc', '12', '14552563369', '123sdf@qq.com', '/src/assets/images/1.png', 'TEACHER', '2025-10-16 22:04:57', '2025-10-16 22:04:57', '123', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('1979079350726078466', 'abcd', 'a906449d5769fa7361d7ecc6aa3f6d28', '李双', '17859666325', '2222510014@s.nbufe.edu.cn', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/8926e237-1add-46f1-a662-43cdfa007951.png', 'STUDENT', '2025-10-17 14:57:41', '2026-02-02 11:11:52', 'abcd', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('1982720631266926594', 'abcde', 'a906449d5769fa7361d7ecc6aa3f6d28', 'adad', '18995956624', '2222510014@s.nbufe.edu.cn', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/4f0bd1bd-3aab-4bdd-94da-86ade64f8054.png', 'TEACHER', '2025-10-27 16:06:50', '2025-11-03 15:00:24', 'abcde', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('1987755992376414210', 'admin', 'e00cf25ad42683b3df678c61f42c6bda', 'max', '18358262541', '2222510014@s.nbufe.edu.cn', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/ae9eccb2-8eda-41ce-8319-9cf7c656c799.png', 'ADMIN', '2025-11-10 13:35:34', '2026-02-02 10:26:16', 'admin', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('2001459426933989378', 'aaaaa', 'a906449d5769fa7361d7ecc6aa3f6d28', '小明', '17815966372', '2eeea@168.com', 'https://hmleadnews-max.oss-cn-hangzhou.aliyuncs.com/a00180bf-d4e6-404b-a08a-061458bd2cb0.png', 'TEACHER', '2025-12-18 09:08:07', '2026-01-16 13:19:53', 'aaaaa', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('2018165850192605186', 'max_12', 'a906449d5769fa7361d7ecc6aa3f6d28', '小红', '17815666235', '2eeea@168.com', '/src/assets/images/1.png', 'TEACHER', '2026-02-02 11:33:29', '2026-02-02 11:33:29', 'max_12', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('2018250806839341057', 'lzy', 'a906449d5769fa7361d7ecc6aa3f6d28', '糖梧桐', '17566935826', 'tangmeng@tt.com', '/src/assets/images/1.png', 'STUDENT', '2026-02-02 17:11:04', '2026-02-02 17:11:04', 'lzy', NULL, NULL, 0, 0);
INSERT INTO `user` VALUES ('2018613228933742594', 'maxx', '9cbf8a4dcb8e30682b927f352d6559a0', 'xxx', '17815966673', 'tangmeng@tt.com', '/src/assets/images/1.png', 'STUDENT', '2026-02-03 17:11:12', '2026-02-03 17:11:12', 'maxx', NULL, NULL, 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
