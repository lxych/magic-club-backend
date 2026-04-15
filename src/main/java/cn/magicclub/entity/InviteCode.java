package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 邀请码实体
 */
@Data
@TableName("mc_invite_code")
public class InviteCode implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 邀请码
     */
    private String code;

    /**
     * 状态: 0-作废 1-有效
     */
    private Integer status;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 作废时间
     */
    private LocalDateTime invalidTime;

    /**
     * 作废原因
     */
    private String invalidReason;
}
