ALTER TABLE `student_whitelist`
ADD COLUMN `role` varchar(20) NOT NULL DEFAULT 'STUDENT' COMMENT '身份(STUDENT/TEACHER)' AFTER `college`;

UPDATE `student_whitelist`
SET `role` = 'STUDENT'
WHERE `role` IS NULL OR TRIM(`role`) = '';
