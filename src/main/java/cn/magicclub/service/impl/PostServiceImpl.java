package cn.magicclub.service.impl;

import cn.magicclub.entity.Post;
import cn.magicclub.entity.User;
import cn.magicclub.mapper.PostMapper;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.service.PostService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 帖子Service实现类
 *
 * @author demo
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Post createPost(Post post) {
        // 设置默认值
        if (post.getViewCount() == null) post.setViewCount(0);
        if (post.getLikeCount() == null) post.setLikeCount(0);
        if (post.getReplyCount() == null) post.setReplyCount(0);
        if (post.getIsTop() == null) post.setIsTop(0);
        if (post.getIsEssence() == null) post.setIsEssence(0);
        if (post.getStatus() == null) post.setStatus(1);

        postMapper.insert(post);
        return post;
    }

    @Override
    @Transactional
    public boolean updatePost(Post post) {
        // 只允许修改标题和内容
        Post updatePost = new Post();
        updatePost.setId(post.getId());
        updatePost.setTitle(post.getTitle());
        updatePost.setContent(post.getContent());
        updatePost.setCategory(post.getCategory());

        return postMapper.updateById(updatePost) > 0;
    }

    @Override
    @Transactional
    public boolean deletePost(Long postId, Long userId) {
        // 查询帖子
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查权限（只能删除自己的帖子，除非是管理员）
        User user = userMapper.selectById(userId);
        if (!post.getUserId().equals(userId) && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("没有权限删除该帖子");
        }

        return postMapper.deleteById(postId) > 0;
    }

    @Override
    public IPage<Post> getPostPage(IPage<Post> page, String category, String keyword) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getStatus, 1);
        wrapper.eq(Post::getDeleted, 0);

        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(Post::getCategory, category);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Post::getTitle, keyword)
                    .or()
                    .like(Post::getContent, keyword));
        }

        // 置顶优先，然后按时间倒序
        wrapper.orderByDesc(Post::getIsTop);
        wrapper.orderByDesc(Post::getIsEssence);
        wrapper.orderByDesc(Post::getCreateTime);

        IPage<Post> resultPage = postMapper.selectPage(page, wrapper);

        // 填充关联数据
        fillPostRelatedData(resultPage.getRecords());

        return resultPage;
    }

    @Override
    public Post getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new RuntimeException("帖子不存在");
        }

        // 填充作者信息
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            post.setAuthorName(user.getDisplayName());
            post.setAuthorAvatar(user.getAvatar());
        }

        // 分类名称
        post.setCategoryName(getCategoryName(post.getCategory()));

        return post;
    }

    @Override
    public void incrementViewCount(Long postId) {
        postMapper.incrementViewCount(postId);
    }

    @Override
    @Transactional
    public boolean likePost(Long postId, Boolean isLike) {
        if (isLike) {
            postMapper.incrementLikeCount(postId);
        } else {
            postMapper.decrementLikeCount(postId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean toggleTop(Long postId, Boolean isTop) {
        return lambdaUpdate()
            .set(Post::getIsTop, isTop ? 1 : 0)
            .eq(Post::getId, postId)
            .update();
    }

    @Override
    @Transactional
    public boolean toggleEssence(Long postId, Boolean isEssence) {
        return lambdaUpdate()
            .set(Post::getIsEssence, isEssence ? 1 : 0)
            .eq(Post::getId, postId)
            .update();
    }

    @Override
    public IPage<Post> getUserPosts(IPage<Post> page, Long userId) {
        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Post::getUserId, userId);
        wrapper.eq(Post::getStatus, 1);
        wrapper.eq(Post::getDeleted, 0);
        wrapper.orderByDesc(Post::getCreateTime);

        return postMapper.selectPage(page, wrapper);
    }

    /**
     * 填充帖子关联数据
     */
    private void fillPostRelatedData(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // 获取所有用户ID
        List<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        // 填充数据
        for (Post post : posts) {
            User user = userMap.get(post.getUserId());
            if (user != null) {
                post.setAuthorName(user.getDisplayName());
                post.setAuthorAvatar(user.getAvatar());
            }
            post.setCategoryName(getCategoryName(post.getCategory()));
        }
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(String category) {
        if (category == null) return "综合";
        switch (category) {
            case "GENERAL": return "综合讨论";
            case "MAGIC": return "魔术技巧";
            case "SHOW": return "演出交流";
            case "RESOURCE": return "资源分享";
            default: return "综合";
        }
    }
}
