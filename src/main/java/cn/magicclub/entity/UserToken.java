package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 Token 实体
 */
@Data
@TableName("mc_user_token")
public class UserToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String token;

    private LocalDateTime loginTime;

    private LocalDateTime expireTime;

    /** 状态: 0-失效 1-有效 */
    private Integer status;
}
