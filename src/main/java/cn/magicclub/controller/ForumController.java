package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.PermissionConstants;
import cn.magicclub.common.Result;
import cn.magicclub.entity.Post;
import cn.magicclub.entity.PostReply;
import cn.magicclub.security.RequirePermission;
import cn.magicclub.service.PostReplyService;
import cn.magicclub.service.PostService;
import cn.magicclub.service.SystemConfigService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛Controller
 *
 * @author demo
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final PostService postService;
    private final PostReplyService postReplyService;
    private final JwtUtil jwtUtil;
    private final SystemConfigService systemConfigService;

    /**
     * 创建帖子
     */
    @PostMapping("/post")
    public Result<Post> createPost(@RequestHeader("Authorization") String token,
                                   @RequestBody Post post) {
        // 检查全员禁言
        if (systemConfigService.isGlobalMute()) {
            return Result.error(403, "系统已开启全员禁言，暂时无法发帖");
        }
        
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        post.setUserId(userId);
        Post createdPost = postService.createPost(post);
        return Result.ok("发布成功", createdPost);
    }

    /**
     * 更新帖子
     */
    @PutMapping("/post/{id}")
    public Result<Boolean> updatePost(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id,
                                      @RequestBody Post post) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        post.setId(id);
        boolean success = postService.updatePost(post);
        return Result.ok("更新成功", success);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/post/{id}")
    public Result<Boolean> deletePost(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean success = postService.deletePost(id, userId);
        return Result.ok("删除成功", success);
    }

    /**
     * 分页查询帖子列表
     */
    @GetMapping("/posts")
    public Result<IPage<Post>> listPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        IPage<Post> page = new Page<>(pageNum, pageSize);
        IPage<Post> resultPage = postService.getPostPage(page, category, keyword);
        return Result.ok(resultPage);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{id}")
    public Result<Map<String, Object>> getPostDetail(@PathVariable Long id) {
        // 增加浏览数
        postService.incrementViewCount(id);

        Post post = postService.getPostDetail(id);

        // 获取回复列表
        List<PostReply> replies = postReplyService.getRepliesByPostId(id);

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("replies", replies);

        return Result.ok(result);
    }

    /**
     * 点赞/取消点赞帖子
     */
    @PostMapping("/post/{id}/like")
    public Result<Boolean> likePost(@PathVariable Long id,
                                    @RequestParam(defaultValue = "true") Boolean isLike) {
        boolean success = postService.likePost(id, isLike);
        return Result.ok(isLike ? "点赞成功" : "取消点赞", success);
    }

    /**
     * 置顶/取消置顶帖子
     * <p>
     * 权限：forum:top（管理员与版主均拥有，支持用户级自定义授权）
     */
    @PostMapping("/post/{id}/top")
    @RequirePermission(PermissionConstants.FORUM_TOP)
    public Result<Boolean> toggleTop(@PathVariable Long id,
                                     @RequestParam("isTop") Boolean isTop) {
        boolean success = postService.toggleTop(id, isTop);
        return Result.ok(isTop ? "置顶成功" : "取消置顶成功", success);
    }

    /**
     * 设为精华/取消精华
     * <p>
     * 权限：forum:essence
     */
    @PostMapping("/post/{id}/essence")
    @RequirePermission(PermissionConstants.FORUM_ESSENCE)
    public Result<Boolean> toggleEssence(@PathVariable Long id,
                                         @RequestParam("isEssence") Boolean isEssence) {
        boolean success = postService.toggleEssence(id, isEssence);
        return Result.ok(isEssence ? "设为精华成功" : "取消精华成功", success);
    }

    /**
     * 获取用户的帖子列表
     */
    @GetMapping("/user/{userId}/posts")
    public Result<IPage<Post>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        IPage<Post> page = new Page<>(pageNum, pageSize);
        IPage<Post> resultPage = postService.getUserPosts(page, userId);
        return Result.ok(resultPage);
    }

    /**
     * 创建回复
     */
    @PostMapping("/post/{postId}/reply")
    public Result<PostReply> createReply(@RequestHeader("Authorization") String token,
                                         @PathVariable Long postId,
                                         @RequestBody PostReply reply) {
        // 检查全员禁言
        if (systemConfigService.isGlobalMute()) {
            return Result.error(403, "系统已开启全员禁言，暂时无法评论");
        }
        
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        reply.setPostId(postId);
        reply.setUserId(userId);
        PostReply createdReply = postReplyService.createReply(reply);
        return Result.ok("回复成功", createdReply);
    }

    /**
     * 删除回复
     */
    @DeleteMapping("/reply/{id}")
    public Result<Boolean> deleteReply(@RequestHeader("Authorization") String token,
                                       @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean success = postReplyService.deleteReply(id, userId);
        return Result.ok("删除成功", success);
    }

    /**
     * 获取帖子的回复列表
     */
    @GetMapping("/post/{postId}/replies")
    public Result<List<PostReply>> getReplies(@PathVariable Long postId) {
        List<PostReply> replies = postReplyService.getRepliesByPostId(postId);
        return Result.ok(replies);
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
