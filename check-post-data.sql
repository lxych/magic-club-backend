-- 检查帖子表中的置顶和精华字段
SELECT id, title, is_top, is_essence, create_time 
FROM mc_post 
ORDER BY create_time DESC 
LIMIT 5;
