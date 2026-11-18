-- 魔术社团网站数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS magic_club 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE magic_club;

-- ============================================
-- 用户表 (包含成员信息)
-- ============================================
DROP TABLE IF EXISTS `mc_user`;
CREATE TABLE `mc_user` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `nickname` VARCHAR(32) COMMENT '昵称',
    `gender` TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    `student_no` VARCHAR(20) COMMENT '学号',
    `enrollment_year` INT COMMENT '入学年份',
    `graduation_year` INT COMMENT '毕业年份',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `email` VARCHAR(100) COMMENT '邮箱',
    `wechat` VARCHAR(50) COMMENT '微信号',
    `department` VARCHAR(50) COMMENT '所属部门',
    `major` VARCHAR(100) COMMENT '所在专业',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `remark` TEXT COMMENT '备注',
    `role` VARCHAR(20) DEFAULT 'MEMBER' COMMENT '角色: ADMIN-管理员 MEMBER-成员 ALUMNI-校友',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    `is_graduated` TINYINT DEFAULT 0 COMMENT '是否毕业: 0-否 1-是',
    `forced_logout` TINYINT DEFAULT 0 COMMENT '是否被强制下线: 0-否 1-是',
    `permissions` TEXT COMMENT '自定义权限列表(JSON数组)，为空时使用角色默认权限',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除: 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_student_no` (`student_no`),
    KEY `idx_enrollment_year` (`enrollment_year`),
    KEY `idx_graduation_year` (`graduation_year`),
    KEY `idx_department` (`department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 论坛帖子表
-- ============================================
DROP TABLE IF EXISTS `mc_post`;
CREATE TABLE `mc_post` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `user_id` BIGINT NOT NULL COMMENT '作者ID',
    `category` VARCHAR(50) DEFAULT 'GENERAL' COMMENT '分类: GENERAL-综合 MAGIC-魔术技巧 SHOW-演出交流 RESOURCE-资源分享',
    `view_count` INT DEFAULT 0 COMMENT '浏览数',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `reply_count` INT DEFAULT 0 COMMENT '回复数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
    `is_essence` TINYINT DEFAULT 0 COMMENT '是否精华: 0-否 1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-草稿 1-已发布 2-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- ============================================
-- 帖子回复表
-- ============================================
DROP TABLE IF EXISTS `mc_post_reply`;
CREATE TABLE `mc_post_reply` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `post_id` BIGINT NOT NULL COMMENT '帖子ID',
    `user_id` BIGINT NOT NULL COMMENT '回复者ID',
    `content` TEXT NOT NULL COMMENT '内容',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父回复ID(用于楼中楼)',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子回复表';

-- ============================================
-- 资源文件表
-- ============================================
DROP TABLE IF EXISTS `mc_resource`;
CREATE TABLE `mc_resource` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(200) COMMENT '原始文件名',
    `description` TEXT COMMENT '文件描述',
    `file_type` VARCHAR(50) COMMENT '文件类型: VIDEO-视频 DOCUMENT-文档 IMAGE-图片 OTHER-其他',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `file_path` VARCHAR(500) COMMENT '文件路径',
    `file_url` VARCHAR(500) COMMENT '访问URL',
    `mime_type` VARCHAR(100) COMMENT 'MIME类型',
    `user_id` BIGINT NOT NULL COMMENT '上传者ID',
    `download_count` INT DEFAULT 0 COMMENT '下载次数',
    `category` VARCHAR(50) DEFAULT 'OTHER' COMMENT '分类: TUTORIAL-教程 SCRIPT-剧本 PROP-道具资料 PERFORMANCE-演出视频 OTHER-其他',
    `is_public` TINYINT DEFAULT 1 COMMENT '是否公开: 0-私有 1-公开',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-待审核 1-已通过 2-已拒绝',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_file_type` (`file_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源文件表';

-- ============================================
-- 纪念墙表(毕业成员)
-- ============================================
DROP TABLE IF EXISTS `mc_memorial`;
CREATE TABLE `mc_memorial` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `graduation_year` INT NOT NULL COMMENT '毕业年份',
    `message` TEXT COMMENT '毕业寄语',
    `achievements` TEXT COMMENT '社团贡献/成就',
    `current_status` VARCHAR(200) COMMENT '当前状态(去向)',
    `contact_public` TINYINT DEFAULT 1 COMMENT '联系方式是否公开: 0-否 1-是',
    `display_order` INT DEFAULT 0 COMMENT '展示顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_graduation_year` (`graduation_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纪念墙表';

-- ============================================
-- 开发维护人员表
-- ============================================
DROP TABLE IF EXISTS `mc_developer`;
CREATE TABLE `mc_developer` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT COMMENT '关联用户ID(可选)',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `role` VARCHAR(50) COMMENT '角色: 创始人/核心开发/UI设计/测试/维护',
    `contributions` TEXT COMMENT '贡献描述',
    `join_time` DATE COMMENT '加入时间',
    `leave_time` DATE COMMENT '离开时间',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否在职: 0-否 1-是',
    `avatar` VARCHAR(500) COMMENT '头像',
    `display_order` INT DEFAULT 0 COMMENT '展示顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开发维护人员表';

-- ============================================
-- 系统配置表
-- ============================================
DROP TABLE IF EXISTS `mc_config`;
CREATE TABLE `mc_config` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- ============================================
-- 操作日志表
-- ============================================
DROP TABLE IF EXISTS `mc_operation_log`;
CREATE TABLE `mc_operation_log` (
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
    KEY `idx_action` (`action`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- 插入初始数据
-- ============================================

-- 插入管理员账号 (密码: admin123)
INSERT INTO `mc_user` (`id`, `username`, `password`, `real_name`, `gender`, `role`, `status`, `create_time`, `update_time`) 
VALUES 
(1, 'admin', '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5E', '管理员', 1, 'ADMIN', 1, NOW(), NOW());

-- 插入部门选项
INSERT INTO `mc_config` (`id`, `config_key`, `config_value`, `description`) VALUES
(1, 'departments', '["组织部","宣传部","外联部","技术部","财务部","表演部"]', '部门列表'),
(2, 'post_categories', '[{"code":"GENERAL","name":"综合讨论"},{"code":"MAGIC","name":"魔术技巧"},{"code":"SHOW","name":"演出交流"},{"code":"RESOURCE","name":"资源分享"}]', '帖子分类'),
(3, 'resource_categories', '[{"code":"TUTORIAL","name":"魔术教程"},{"code":"SCRIPT","name":"剧本资料"},{"code":"PROP","name":"道具制作"},{"code":"PERFORMANCE","name":"演出视频"},{"code":"OTHER","name":"其他资源"}]', '资源分类');

-- 插入开发维护人员示例
INSERT INTO `mc_developer` (`id`, `name`, `role`, `contributions`, `join_time`, `is_active`, `display_order`) VALUES
(1, '创始人', '创始人/架构设计', '项目发起，整体架构设计', '2024-01-01', 1, 1),
(2, '核心开发', '后端开发', '负责后端API开发', '2024-01-15', 1, 2),
(3, '前端开发', '前端开发/UI设计', '负责前端页面开发', '2024-02-01', 1, 3);
