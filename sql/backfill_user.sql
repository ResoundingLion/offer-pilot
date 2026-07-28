-- ============================================================
-- 补数据：为所有已有 user_account 创建对应的 user 记录
--
-- 背景：2026-07-23 之前注册的老用户，user_account 直接指向自己，
--       user 表从未写入。现在两张表需要真正连通。
--
-- 执行方式：
--   docker exec -i offer-mysql mysql -uroot -p123456 < backfill_user.sql
-- ============================================================

USE offer_user;

-- 为 user_account 中没有对应 user 记录的账号补数据
INSERT INTO `user` (id, name, email, phone, created_at, updated_at)
SELECT
    ua.user_id,
    ua.username,
    NULL,
    NULL,
    NOW(),
    NOW()
FROM offer_auth.user_account ua
WHERE NOT EXISTS (
    SELECT 1 FROM `user` u WHERE u.id = ua.user_id
);

-- 检查补了多少条
SELECT CONCAT('已补 ', ROW_COUNT(), ' 条 user 记录') AS result;
