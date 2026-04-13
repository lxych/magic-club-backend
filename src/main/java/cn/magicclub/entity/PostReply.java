package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子回复实体类
 *
 * @author demo
 * @since 2025-04-13
 */
@Data
@TableName("mc_post_reply")
public class PostReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 回复者ID
     */
    private Long userId;

    /**
     * 内容
     */
    private String content;

    /**
     * 父回复ID(用于楼中楼)
     */
    private Long parentId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // ==================== 非数据库字段 ====================

    /**
     * 回复者昵称
     */
    @TableField(exist = false)
    private String authorName;

    /**
     * 回复者头像
     */
    @TableField(exist = false)
    private String authorAvatar;
}
