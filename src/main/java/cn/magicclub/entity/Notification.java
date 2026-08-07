package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知消息实体类
 *
 * @author demo
 * @since 2025-08-05
 */
@Data
@TableName("mc_notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: RESOURCE_APPROVED/RESOURCE_REJECTED/SYSTEM
     */
    private String type;

    /**
     * 关联资源ID
     */
    private Long relatedId;

    /**
     * 发送者用户ID（公告发送人）
     */
    private Long senderId;

    /**
     * 是否已读: 0未读 1已读
     */
    private Integer isRead;

    /**
     * 附件JSON: [{"name":"文件名","url":"/uploads/xxx","type":"image/png"}]
     */
    private String attachments;

    /**
     * 可撤回截止时间（公告发送后2分钟内）
     */
    private LocalDateTime canRecallUntil;

    /**
     * 是否已撤回: 0正常 1已撤回
     */
    private Integer isRecalled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
