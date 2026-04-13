package cn.magicclub.service;

import cn.magicclub.entity.PostReply;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 帖子回复Service接口
 *
 * @author demo
 * @since 2025-04-13
 */
public interface PostReplyService extends IService<PostReply> {

    /**
     * 创建回复
     */
    PostReply createReply(PostReply reply);

    /**
     * 删除回复
     */
    boolean deleteReply(Long replyId, Long userId);

    /**
     * 获取帖子的回复列表
     */
    List<PostReply> getRepliesByPostId(Long postId);

    /**
     * 获取楼中楼回复
     */
    List<PostReply> getSubReplies(Long parentId);

    /**
     * 点赞回复
     */
    boolean likeReply(Long replyId);
}
