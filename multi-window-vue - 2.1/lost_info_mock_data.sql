-- lost_info 表模拟数据插入脚本
-- 字段列表：id, user_id, name, type, location, description, images, contact_phone, contact_email, status, reject_reason, view_count, publish_time, update_time

-- 第一条数据：钱包
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  1, 101, '黑色钱包', '钱包/卡包', '图书馆二楼自习区', '内含身份证、学生证和银行卡若干，表面有Nike标志',
  '["wallet1.jpg", "wallet2.jpg"]', '13800138001', 'user1@example.com', 'APPROVED', null, 156,
  '2024-01-15 10:30:00', '2024-01-15 10:30:00'
);

-- 第二条数据：手机
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  2, 102, 'iPhone 13 Pro', '电子设备', '教学楼A栋305教室', '深空灰色，戴有透明手机壳，屏幕有轻微划痕',
  '["phone1.jpg", "phone2.jpg"]', '13900139002', 'user2@example.com', 'APPROVED', null, 234,
  '2024-01-14 15:45:00', '2024-01-14 15:45:00'
);

-- 第三条数据：眼镜
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  3, 103, '黑色框架眼镜', '配饰', '食堂一楼', '金属框架，度数约300度，比较轻',
  '["glasses1.jpg"]', '13700137003', 'user3@example.com', 'PENDING', null, 89,
  '2024-01-16 12:10:00', '2024-01-16 12:10:00'
);

-- 第四条数据：钥匙串
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  4, 104, '钥匙串', '生活用品', '操场看台', '包含宿舍钥匙和自行车钥匙，挂有篮球形状的钥匙扣',
  '["keys1.jpg", "keys2.jpg"]', '13600136004', 'user4@example.com', 'SOLVED', null, 45,
  '2024-01-12 18:20:00', '2024-01-13 09:30:00'
);

-- 第五条数据：书包
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  5, 105, '灰色双肩包', '箱包', '校车停靠点', '小米品牌，内有课本和笔记本，侧面有水杯袋',
  '["bag1.jpg", "bag2.jpg", "bag3.jpg"]', '13500135005', 'user5@example.com', 'APPROVED', null, 178,
  '2024-01-15 19:40:00', '2024-01-15 19:40:00'
);

-- 第六条数据：雨伞
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  6, 106, '蓝色折叠伞', '生活用品', '实验楼B栋走廊', '天堂伞品牌，伞柄有轻微磨损',
  '["umbrella1.jpg"]', '13400134006', 'user6@example.com', 'REJECTED', '图片不够清晰，请上传更清晰的物品照片', 23,
  '2024-01-16 08:15:00', '2024-01-16 10:20:00'
);

-- 第七条数据：手表
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  7, 107, '黑色运动手表', '配饰', '健身房储物柜', '华为GT3，黑色表带，有心率监测功能',
  '["watch1.jpg", "watch2.jpg"]', '13300133007', 'user7@example.com', 'APPROVED', null, 167,
  '2024-01-14 20:30:00', '2024-01-14 20:30:00'
);

-- 第八条数据：课本
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  8, 108, '高等数学教材', '书籍', '教学楼C栋203教室', '第七版上下册，书角有折痕，内有笔记',
  '["book1.jpg"]', '13200132008', 'user8@example.com', 'PENDING', null, 67,
  '2024-01-16 14:25:00', '2024-01-16 14:25:00'
);

-- 第九条数据：耳机
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  9, 109, 'AirPods Pro', '电子设备', '图书馆一楼咖啡厅', '白色，带有充电盒，序列号前几位为C39',
  '["earphones1.jpg", "earphones2.jpg"]', '13100131009', 'user9@example.com', 'APPROVED', null, 198,
  '2024-01-15 16:50:00', '2024-01-15 16:50:00'
);

-- 第十条数据：外套
INSERT INTO lost_info (
  id, user_id, name, type, location, description, images,
  contact_phone, contact_email, status, reject_reason, view_count,
  publish_time, update_time
) VALUES (
  10, 110, '黑色羽绒服', '服装', '体育馆', 'Canada Goose品牌，M码，左袖口有标志',
  '["jacket1.jpg", "jacket2.jpg"]', '13000130010', 'user10@example.com', 'SOLVED', null, 76,
  '2024-01-13 15:10:00', '2024-01-14 11:45:00'
);

-- 如果需要重置自增ID序列（MySQL）
-- ALTER TABLE lost_info AUTO_INCREMENT = 11;

-- 如果需要重置自增ID序列（PostgreSQL）
-- SELECT setval('lost_info_id_seq', 11);