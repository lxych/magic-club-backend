-- 检查特定帖子的置顶和精华状态
-- 请将 1 替换为实际的帖子ID
SELECT id, title, is_top, is_essence 
FROM mc_post 
WHERE id = 1;

-- 更新测试：手动设置置顶
-- UPDATE mc_post SET is_top = 1 WHERE id = 1;

-- 更新测试：手动设置精华
-- UPDATE mc_post SET is_essence = 1 WHERE id = 1;
