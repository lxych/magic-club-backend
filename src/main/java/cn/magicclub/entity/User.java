package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author demo
 * @since 2025-04-13
 */
@Data
@TableName("mc_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(加密)
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别: 0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 学号
     */
    private String studentNo;

    /**
     * 入学年份
     */
    private Integer enrollmentYear;

    /**
     * 毕业年份
     */
    private Integer graduationYear;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 所在专业
     */
    private String major;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 备注
     */
    private String remark;

    /**
     * 角色: ADMIN-管理员 MEMBER-成员 ALUMNI-校友
     */
    private String role;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 是否毕业: 0-否 1-是
     */
    @TableField("is_graduated")
    private Integer isGraduated;

    /**
     * 是否被强制下线: 0-否 1-是
     */
    @TableField("forced_logout")
    private Integer forcedLogout;

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
     * 性别名称
     */
    @TableField(exist = false)
    private String genderName;

    /**
     * 角色名称
     */
    @TableField(exist = false)
    private String roleName;

    /**
     * 状态名称
     */
    @TableField(exist = false)
    private String statusName;
    
    /**
     * 邀请码（注册时使用，不存入数据库）
     */
    @TableField(exist = false)
    private String inviteCode;
}
