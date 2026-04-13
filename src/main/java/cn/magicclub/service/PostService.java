package cn.magicclub.service;

import cn.magicclub.entity.Post;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 帖子Service接口
 *
 * @author demo
 * @since 2025-04-13
 */
public interface PostService extends IService<Post> {

    /**
     * 创建帖子
     */
    Post createPost(Post post);

    /**
     * 更新帖子
     */
    boolean updatePost(Post post);

    /**
     * 删除帖子
     */
    boolean deletePost(Long postId, Long userId);

    /**
     * 分页查询帖子列表
     */
    IPage<Post> getPostPage(IPage<Post> page, String category, String keyword);

    /**
     * 获取帖子详情
     */
    Post getPostDetail(Long postId);

    /**
     * 增加浏览数
     */
    void incrementViewCount(Long postId);

    /**
     * 点赞帖子
     */
    boolean likePost(Long postId);

    /**
     * 置顶/取消置顶
     */
    boolean toggleTop(Long postId, Boolean isTop);

    /**
     * 设为精华/取消精华
     */
    boolean toggleEssence(Long postId, Boolean isEssence);

    /**
     * 获取用户的帖子列表
     */
    IPage<Post> getUserPosts(IPage<Post> page, Long userId);
}
