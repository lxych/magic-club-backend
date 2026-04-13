package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 开发维护人员实体类
 *
 * @author demo
 * @since 2025-04-13
 */
@Data
@TableName("mc_developer")
public class Developer implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联用户ID(可选)
     */
    private Long userId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 角色: 创始人/核心开发/UI设计/测试/维护
     */
    private String role;

    /**
     * 贡献描述
     */
    private String contributions;

    /**
     * 加入时间
     */
    private LocalDate joinTime;

    /**
     * 离开时间
     */
    private LocalDate leaveTime;

    /**
     * 是否在职: 0-否 1-是
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 头像
     */
    private String avatar;

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
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    // ==================== 非数据库字段 ====================

    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;
}
