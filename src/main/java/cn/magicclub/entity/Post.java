package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 *
 * @author demo
 * @since 2025-04-13
 */
@Data
@TableName("mc_post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 分类: GENERAL-综合 MAGIC-魔术技巧 SHOW-演出交流 RESOURCE-资源分享
     */
    private String category;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 是否置顶: 0-否 1-是
     */
    @TableField("is_top")
    @JsonProperty("isTop")
    private Integer isTop;

    /**
     * 是否精华: 0-否 1-是
     */
    @TableField("is_essence")
    @JsonProperty("isEssence")
    private Integer isEssence;

    /**
     * 状态: 0-草稿 1-已发布 2-已删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // ==================== 非数据库字段 ====================

    /**
     * 作者昵称
     */
    @TableField(exist = false)
    private String authorName;

    /**
     * 作者头像
     */
    @TableField(exist = false)
    private String authorAvatar;

    /**
     * 分类名称
     */
    @TableField(exist = false)
    private String categoryName;
}
