-- 创建邀请码表
CREATE TABLE IF NOT EXISTS mc_invite_code (
    id BIGINT NOT NULL COMMENT 'ID',
    code VARCHAR(50) NOT NULL COMMENT '邀请码',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-作废 1-有效',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME COMMENT '创建时间',
    invalid_time DATETIME COMMENT '作废时间',
    invalid_reason VARCHAR(200) COMMENT '作废原因',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请码表';

-- 创建系统配置表
CREATE TABLE IF NOT EXISTS mc_system_config (
    config_key VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    description VARCHAR(200) COMMENT '说明',
    PRIMARY KEY (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化邀请码（默认邀请码）
INSERT INTO mc_invite_code (id, code, status, create_by, create_time) 
VALUES (1, '&20082026', 1, 1, NOW());

-- 初始化系统配置
INSERT INTO mc_system_config (config_key, config_value, description) VALUES 
('global_mute', 'false', '全员禁言开关'),
('valid_invite_code', '&20082026', '当前有效邀请码');

-- 修改用户表，添加强制下线字段
ALTER TABLE mc_user ADD COLUMN IF NOT EXISTS forced_logout TINYINT DEFAULT 0 COMMENT '是否被强制下线: 0-否 1-是';

-- 设置管理员不被强制下线
UPDATE mc_user SET forced_logout = 0 WHERE role = 'ADMIN';

-- 创建用户登录记录表（可选，用于强制下线时清除token）
CREATE TABLE IF NOT EXISTS mc_user_token (
    id BIGINT NOT NULL COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(500) COMMENT '登录token',
    login_time DATETIME COMMENT '登录时间',
    expire_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-失效 1-有效',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录Token记录';
