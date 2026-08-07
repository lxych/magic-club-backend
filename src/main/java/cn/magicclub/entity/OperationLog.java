package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 *
 * @author demo
 * @since 2025-08-06
 */
@Data
@TableName("mc_operation_log")
public class OperationLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 操作人ID
     */
    private Long userId;

    /**
     * 操作人用户名
     */
    private String username;

    /**
     * 操作类型
     * GRADUATE / CANCEL_GRADUATE / FORCE_LOGOUT / CANCEL_USER /
     * AUDIT_APPROVE / AUDIT_REJECT / SEND_ANNOUNCEMENT / RECALL_ANNOUNCEMENT /
     * CREATE_RESOURCE / DELETE_RESOURCE / MUTE_USER / UNMUTE_USER
     */
    private String action;

    /**
     * 操作对象描述（如用户名、资源名）
     */
    private String target;

    /**
     * 操作对象ID
     */
    private String targetId;

    /**
     * 详细信息（JSON或文本）
     */
    private String detail;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
