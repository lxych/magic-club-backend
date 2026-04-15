package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 纪念墙-历史事件实体类
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
     * 事件标题
     */
    private String title;

    /**
     * 事件内容/介绍
     */
    private String content;

    /**
     * 事件日期
     */
    private LocalDate eventDate;

    /**
     * 年份（用于分组展示）
     */
    private Integer year;

    /**
     * 图片URL，多个用逗号分隔
     */
    private String imageUrls;

    /**
     * 创建者ID
     */
    private Long createBy;

    /**
     * 创建者名称（非数据库字段）
     */
    @TableField(exist = false)
    private String createByName;

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
}
