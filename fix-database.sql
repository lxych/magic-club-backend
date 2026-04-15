-- 检查并添加 forced_logout 字段（MySQL 8.0 语法）
SET @exist := (
    SELECT COUNT(*) FROM information_schema.columns 
    WHERE table_name = 'mc_user' 
    AND column_name = 'forced_logout'
    AND table_schema = DATABASE()
);

SET @sql := IF(@exist = 0, 
    'ALTER TABLE mc_user ADD COLUMN forced_logout TINYINT DEFAULT 0 COMMENT "是否被强制下线: 0-否 1-是"',
    'SELECT "Column already exists"'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查 mc_system_config 是否已有数据，避免重复插入
DELETE FROM mc_system_config WHERE config_key IN ('global_mute', 'valid_invite_code');

-- 重新插入系统配置
INSERT INTO mc_system_config (config_key, config_value, description) VALUES 
('global_mute', 'false', '全员禁言开关'),
('valid_invite_code', '&20082026', '当前有效邀请码');

-- 确保管理员 forced_logout 为 0
UPDATE mc_user SET forced_logout = 0 WHERE role = 'ADMIN';

-- 查看表结构确认
DESCRIBE mc_user;
