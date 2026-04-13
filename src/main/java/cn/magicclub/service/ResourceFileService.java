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
    ResourceFile uploadFile(MultipartFile file, Long userId, String description, String category);

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
     * 增加下载次数
     */
    void incrementDownloadCount(Long fileId);
}
