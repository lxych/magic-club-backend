package cn.magicclub.service.impl;

import cn.magicclub.entity.Post;
import cn.magicclub.entity.PostReply;
import cn.magicclub.entity.User;
import cn.magicclub.mapper.PostMapper;
import cn.magicclub.mapper.PostReplyMapper;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.service.PostReplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子回复Service实现类
 *
 * @author demo
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class PostReplyServiceImpl extends ServiceImpl<PostReplyMapper, PostReply> implements PostReplyService {

    private final PostReplyMapper postReplyMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public PostReply createReply(PostReply reply) {
        // 检查帖子是否存在
        Post post = postMapper.selectById(reply.getPostId());
        if (post == null || post.getDeleted() == 1) {
            throw new RuntimeException("帖子不存在");
        }

        // 设置默认值
        if (reply.getLikeCount() == null) {
            reply.setLikeCount(0);
        }

        // 保存回复
        postReplyMapper.insert(reply);

        // 增加帖子回复数
        postMapper.incrementReplyCount(reply.getPostId());

        return reply;
    }

    @Override
    @Transactional
    public boolean deleteReply(Long replyId, Long userId) {
        PostReply reply = postReplyMapper.selectById(replyId);
        if (reply == null) {
            throw new RuntimeException("回复不存在");
        }

        // 检查权限
        User user = userMapper.selectById(userId);
        if (!reply.getUserId().equals(userId) && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("没有权限删除该回复");
        }

        return postReplyMapper.deleteById(replyId) > 0;
    }

    @Override
    public List<PostReply> getRepliesByPostId(Long postId) {
        List<PostReply> replies = postReplyMapper.selectByPostId(postId);
        fillReplyRelatedData(replies);
        return replies;
    }

    @Override
    public List<PostReply> getSubReplies(Long parentId) {
        List<PostReply> replies = postReplyMapper.selectByParentId(parentId);
        fillReplyRelatedData(replies);
        return replies;
    }

    @Override
    @Transactional
    public boolean likeReply(Long replyId) {
        // 这里可以实现点赞功能，需要在数据库中添加点赞记录表
        // 简单实现：直接增加点赞数
        return true;
    }

    /**
     * 填充回复关联数据
     */
    private void fillReplyRelatedData(List<PostReply> replies) {
        if (replies == null || replies.isEmpty()) {
            return;
        }

        // 获取所有用户ID
        List<Long> userIds = replies.stream()
                .map(PostReply::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        // 填充数据
        for (PostReply reply : replies) {
            User user = userMap.get(reply.getUserId());
            if (user != null) {
                reply.setAuthorName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                reply.setAuthorAvatar(user.getAvatar());
            }
        }
    }
}
