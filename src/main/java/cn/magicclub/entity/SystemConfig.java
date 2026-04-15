package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 系统配置实体
 */
@Data
@TableName("mc_system_config")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键
     */
    @TableId
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 说明
     */
    private String description;
}
