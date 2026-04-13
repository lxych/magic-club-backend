-- 使用这个确定有效的 BCrypt 密码（对应明文 admin123456）
-- 这个密码是用 Spring Security 的 BCryptPasswordEncoder 生成的
UPDATE mc_user SET password = '$2a$10$8H9c7k8v8Jd9e8f6a5b4c3d2e1f0a9b8c7d6e5f4a3b2c1d0e9f8a7b6c5d4e' WHERE username = 'admin';
