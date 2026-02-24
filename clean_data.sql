-- Data Cleaning Script for Migration
-- Purpose: Migrate legacy data values to new dictionary codes

-- 1. Migrate Lost Info Types (lost_info.type)
-- Old: 1=电子产品, 2=生活用品, 3=证件, 4=其他
-- New: ELECTRONICS, DAILY_NECESSITIES, ID_CARD, OTHER, BOOKS, CLOTHES

UPDATE lost_info SET type = 'ELECTRONICS' WHERE type = '1' OR type = '电子产品';
UPDATE lost_info SET type = 'DAILY_NECESSITIES' WHERE type = '2' OR type = '生活用品';
UPDATE lost_info SET type = 'ID_CARD' WHERE type = '3' OR type = '证件';
UPDATE lost_info SET type = 'OTHER' WHERE type = '4' OR type = '其他';
UPDATE lost_info SET type = 'BOOKS' WHERE type = '书籍文具';
UPDATE lost_info SET type = 'CLOTHES' WHERE type = '衣物';

-- 2. Migrate Find Info Types (find_info.type)
UPDATE find_info SET type = 'ELECTRONICS' WHERE type = '1' OR type = '电子产品';
UPDATE find_info SET type = 'DAILY_NECESSITIES' WHERE type = '2' OR type = '生活用品';
UPDATE find_info SET type = 'ID_CARD' WHERE type = '3' OR type = '证件';
UPDATE find_info SET type = 'OTHER' WHERE type = '4' OR type = '其他';
UPDATE find_info SET type = 'BOOKS' WHERE type = '书籍文具';
UPDATE find_info SET type = 'CLOTHES' WHERE type = '衣物';

-- 3. Migrate Lost Info Status (lost_info.status)
-- Old: 0=寻找中, 1=已寻回, 2=已认领, 3=已取消
-- New: PENDING, APPROVED, REJECTED, SOLVED
-- Logic: 
-- 0 (Searching) -> APPROVED (Published and active)
-- 1 (Found) -> SOLVED
-- 2 (Claimed) -> SOLVED
-- 3 (Cancelled) -> REJECTED (or SOLVED, depending on interpretation, mapping to REJECTED/Cancelled equivalent)
-- Note: 'PENDING' is for new items waiting approval. Old items in '0' are likely already visible.

UPDATE lost_info SET status = 'APPROVED' WHERE status = '0' OR status = '寻找中';
UPDATE lost_info SET status = 'SOLVED' WHERE status = '1' OR status = '已寻回';
UPDATE lost_info SET status = 'SOLVED' WHERE status = '2' OR status = '已认领';
UPDATE lost_info SET status = 'REJECTED' WHERE status = '3' OR status = '已取消';

-- 4. Migrate Find Info Status (find_info.status)
UPDATE find_info SET status = 'APPROVED' WHERE status = '0' OR status = '寻找中';
UPDATE find_info SET status = 'SOLVED' WHERE status = '1' OR status = '已寻回';
UPDATE find_info SET status = 'SOLVED' WHERE status = '2' OR status = '已认领';
UPDATE find_info SET status = 'REJECTED' WHERE status = '3' OR status = '已取消';

-- 5. Migrate User Roles (user.role)
-- Ensure consistency if numeric values were used (assuming 0=Student, 1=Teacher, 2=Admin for example, though usually strings)
-- If current roles are 'student', 'teacher', ensure uppercase
UPDATE user SET role = 'STUDENT' WHERE role = 'student' OR role = '0';
UPDATE user SET role = 'TEACHER' WHERE role = 'teacher' OR role = '1';
UPDATE user SET role = 'ADMIN' WHERE role = 'admin' OR role = '2';

-- 6. Migrate User Status (user.status)
-- 0=Normal, 1=Banned. Usually no change needed if compatible.

