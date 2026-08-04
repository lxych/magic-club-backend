package cn.magicclub.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资源文件实体类
 *
 * @author demo
 * @since 2025-04-13
 */
@Data
@TableName("mc_resource")
public class ResourceFile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文件名
     */
    private String name;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 文件类型: VIDEO-视频 DOCUMENT-文档 IMAGE-图片 OTHER-其他
     */
    private String fileType;

    /**
     * 文件大小(字节)
     */
    private Long fileSize;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 访问URL
     */
    private String fileUrl;

    /**
     * MIME类型
     */
    private String mimeType;

    /**
     * 上传者ID
     */
    private Long userId;

    /**
     * 下载次数
     */
    private Integer downloadCount;

    /**
     * 分类: TUTORIAL-教程 SCRIPT-剧本 PROP-道具资料 PERFORMANCE-演出视频 OTHER-其他
     */
    private String category;

    /**
     * 是否公开: 0-私有 1-公开
     */
    @TableField("is_public")
    private Integer isPublic;

    /**
     * 状态: 0-待审核 1-已通过 2-已拒绝
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

    /**
     * 审批人ID
     */
    private Long auditorId;

    /**
     * 是否允许下载: 0-仅查看 1-可下载
     */
    private Integer allowDownload;

    // ==================== 非数据库字段 ====================

    /**
     * 上传者昵称
     */
    @TableField(exist = false)
    private String uploaderName;

    /**
     * 审批人名称
     */
    @TableField(exist = false)
    private String auditorName;

    /**
     * 文件大小格式化
     */
    @TableField(exist = false)
    private String fileSizeFormatted;

    /**
     * 分类名称
     */
    @TableField(exist = false)
    private String categoryName;
}
