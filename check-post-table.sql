-- 检查帖子表字段
DESCRIBE mc_post;

-- 检查是否有 is_top 和 is_essence 字段
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM information_schema.COLUMNS 
WHERE TABLE_NAME = 'mc_post' 
AND TABLE_SCHEMA = DATABASE()
AND COLUMN_NAME IN ('is_top', 'is_essence');
