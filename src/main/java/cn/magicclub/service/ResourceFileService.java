package cn.magicclub.service;

import cn.magicclub.entity.ResourceFile;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 资源文件Service接口
 *
 * @author demo
 * @since 2025-04-13
 */
public interface ResourceFileService extends IService<ResourceFile> {

    /**
     * 上传文件
     */
    ResourceFile uploadFile(MultipartFile file, Long userId, String description, String category, Integer allowDownload);

    /**
     * 删除文件
     */
    boolean deleteFile(Long fileId, Long userId);

    /**
     * 分页查询资源列表
     */
    IPage<ResourceFile> getResourcePage(IPage<ResourceFile> page, String category, String fileType, String keyword);

    /**
     * 获取资源详情
     */
    ResourceFile getResourceDetail(Long fileId);

    /**
     * 下载文件
     */
    void downloadFile(Long fileId, HttpServletResponse response);

    /**
     * 获取用户的资源列表
     */
    IPage<ResourceFile> getUserResources(IPage<ResourceFile> page, Long userId);

    /**
     * 审核资源
     */
    boolean auditResource(Long fileId, Integer status);

    /**
     * 审核资源（记录审批人）
     */
    boolean auditResource(Long fileId, Integer status, Long auditorId, Integer allowDownload);

    /**
     * 预览文件（inline）
     */
    void previewFile(Long fileId, HttpServletResponse response);

    /**
     * 按状态查资源列表（管理员）
     */
    IPage<ResourceFile> getResourcesByStatus(IPage<ResourceFile> page, Integer status);

    /**
     * 取得待审批资源列表
     */
    IPage<ResourceFile> getPendingResources(IPage<ResourceFile> page);

    /**
     * 增加下载次数
     */
    void incrementDownloadCount(Long fileId);

    /**
     * OSS 直传完成后回调保存记录
     * <p>
     * 前端通过 STS Token 直传到 OSS 后，调用此接口创建 ResourceFile 数据库记录。
     *
     * @param userId      上传者 ID
     * @param fileName    原始文件名
     * @param ossUrl      OSS 完整 URL
     * @param fileSize    文件大小（字节）
     * @param description 文件描述
     * @param category    资源分类
     * @return 创建的 ResourceFile 记录
     */
    ResourceFile completeOssUpload(Long userId, String fileName, String ossUrl, Long fileSize,
                                   String description, String category);
}
