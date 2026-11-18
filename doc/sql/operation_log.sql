-- 操作日志表
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
