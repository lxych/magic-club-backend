-- 更新 admin 用户密码为 BCrypt 加密格式
-- 密码: admin123456
UPDATE mc_user SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO' WHERE username = 'admin';
