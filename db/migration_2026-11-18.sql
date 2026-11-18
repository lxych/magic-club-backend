-- ============================================================
-- 增量修复脚本：2026-11-18
-- 适用对象：已经建过库、但不是用最新 init.sql 初始化的环境
-- 作用：
--   1. 补齐 mc_user 与实体类不一致的字段（permissions 缺失会让所有用户查询报
--      Unknown column 'permissions'，进而导致 JWT 过滤器抛异常、所有已登录请求 401）
--   2. 补建操作日志表（OperationLog 实体对应的 mc_operation_log）
-- 执行方式：
--   mysql -uroot -proot magic_club -e "source D:/path/migration_2026-11-18.sql"
-- 幂等性：字段存在时会跳过，可重复执行
-- ============================================================

USE magic_club;

-- ---------- 1. mc_user 补齐字段 ----------
SET @db := DATABASE();

SET @s := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mc_user' AND COLUMN_NAME = 'nickname') = 0,
    'ALTER TABLE `mc_user` ADD COLUMN `nickname` VARCHAR(32) NULL COMMENT ''昵称''',
    'SELECT ''nickname 已存在，跳过'''));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mc_user' AND COLUMN_NAME = 'forced_logout') = 0,
    'ALTER TABLE `mc_user` ADD COLUMN `forced_logout` TINYINT DEFAULT 0 COMMENT ''是否被强制下线: 0-否 1-是''',
    'SELECT ''forced_logout 已存在，跳过'''));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @s := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mc_user' AND COLUMN_NAME = 'permissions') = 0,
    'ALTER TABLE `mc_user` ADD COLUMN `permissions` TEXT NULL COMMENT ''自定义权限列表(JSON数组)，为空时使用角色默认权限''',
    'SELECT ''permissions 已存在，跳过'''));
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------- 2. 操作日志表 ----------
CREATE TABLE IF NOT EXISTS `mc_operation_log` (
    `id` BIGINT NOT NULL COMMENT '主键（雪花ID）',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `username` VARCHAR(64) DEFAULT NULL COMMENT '操作人用户名',
    `action` VARCHAR(32) DEFAULT NULL COMMENT '操作类型',
    `target` VARCHAR(255) DEFAULT NULL COMMENT '操作对象描述',
    `target_id` VARCHAR(64) DEFAULT NULL COMMENT '操作对象ID',
    `detail` TEXT DEFAULT NULL COMMENT '详细信息',
    `ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_action` (`action`),
    INDEX `idx_username` (`username`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ---------- 3. 校验 ----------
SELECT COLUMN_NAME FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mc_user'
   AND COLUMN_NAME IN ('nickname','forced_logout','permissions');

SELECT TABLE_NAME FROM information_schema.TABLES
 WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'mc_operation_log';
