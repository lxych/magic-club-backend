package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 纪念墙实体类
 *
 * @author demo
 * @since 2025-04-13
 */
@Data
@TableName("mc_memorial")
public class Memorial implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 毕业寄语
     */
    private String message;

    /**
     * 社团贡献/成就
     */
    private String achievements;

    /**
     * 当前状态(去向)
     */
    private String currentStatus;

    /**
     * 联系方式是否公开: 0-否 1-是
     */
    @TableField("contact_public")
    private Integer contactPublic;

    /**
     * 展示顺序
     */
    private Integer displayOrder;

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
     * 用户姓名
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 用户头像
     */
    @TableField(exist = false)
    private String userAvatar;

    /**
     * 所属部门
     */
    @TableField(exist = false)
    private String department;

    /**
     * 专业
     */
    @TableField(exist = false)
    private String major;

    /**
     * 联系电话
     */
    @TableField(exist = false)
    private String phone;

    /**
     * 邮箱
     */
    @TableField(exist = false)
    private String email;
}
