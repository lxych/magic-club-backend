package cn.magicclub.service.impl;

import cn.magicclub.entity.ResourceFile;
import cn.magicclub.entity.User;
import cn.magicclub.mapper.ResourceFileMapper;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.service.ResourceFileService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 资源文件Service实现类
 *
 * @author demo
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class ResourceFileServiceImpl extends ServiceImpl<ResourceFileMapper, ResourceFile> implements ResourceFileService {

    private final ResourceFileMapper resourceFileMapper;
    private final UserMapper userMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    // 允许的文件类型
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp",
            "mp4", "avi", "mov", "wmv",
            "doc", "docx", "pdf", "txt", "ppt", "pptx", "xls", "xlsx",
            "zip", "rar"
    );

    @Override
    @Transactional
    public ResourceFile uploadFile(MultipartFile file, Long userId, String description, String category) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 获取文件信息
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new RuntimeException("文件名不能为空");
        }

        // 检查文件扩展名
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_TYPES.contains(extension)) {
            throw new RuntimeException("不支持的文件类型: " + extension);
        }

        // 生成文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = datePath + "/" + fileName;
        String fullPath = uploadPath + relativePath;

        // 创建目录
        File dir = new File(uploadPath + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        try {
            File dest = new File(fullPath);
            file.transferTo(dest);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }

        // 保存数据库记录
        ResourceFile resourceFile = new ResourceFile();
        resourceFile.setName(fileName);
        resourceFile.setOriginalName(originalFilename);
        resourceFile.setDescription(description);
        resourceFile.setFileType(getFileType(extension));
        resourceFile.setFileSize(file.getSize());
        resourceFile.setFilePath(fullPath);
        resourceFile.setFileUrl("/uploads/" + relativePath);
        resourceFile.setMimeType(file.getContentType());
        resourceFile.setUserId(userId);
        resourceFile.setDownloadCount(0);
        resourceFile.setCategory(category);
        resourceFile.setIsPublic(1);
        resourceFile.setStatus(1); // 默认已通过

        resourceFileMapper.insert(resourceFile);

        return resourceFile;
    }

    @Override
    @Transactional
    public boolean deleteFile(Long fileId, Long userId) {
        ResourceFile resourceFile = resourceFileMapper.selectById(fileId);
        if (resourceFile == null) {
            throw new RuntimeException("文件不存在");
        }

        // 检查权限
        User user = userMapper.selectById(userId);
        if (!resourceFile.getUserId().equals(userId) && !"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("没有权限删除该文件");
        }

        // 删除物理文件
        Path path = Paths.get(resourceFile.getFilePath());
        File file = path.toFile();
        if (file.exists()) {
            file.delete();
        }

        return resourceFileMapper.deleteById(fileId) > 0;
    }

    @Override
    public IPage<ResourceFile> getResourcePage(IPage<ResourceFile> page, String category, String fileType, String keyword) {
        LambdaQueryWrapper<ResourceFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceFile::getStatus, 1);
        wrapper.eq(ResourceFile::getIsPublic, 1);
        wrapper.eq(ResourceFile::getDeleted, 0);

        // 分类筛选
        if (StringUtils.hasText(category)) {
            wrapper.eq(ResourceFile::getCategory, category);
        }

        // 文件类型筛选
        if (StringUtils.hasText(fileType)) {
            wrapper.eq(ResourceFile::getFileType, fileType);
        }

        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(ResourceFile::getName, keyword)
                    .or()
                    .like(ResourceFile::getOriginalName, keyword)
                    .or()
                    .like(ResourceFile::getDescription, keyword));
        }

        wrapper.orderByDesc(ResourceFile::getCreateTime);

        IPage<ResourceFile> resultPage = resourceFileMapper.selectPage(page, wrapper);
        fillResourceRelatedData(resultPage.getRecords());

        return resultPage;
    }

    @Override
    public ResourceFile getResourceDetail(Long fileId) {
        ResourceFile resourceFile = resourceFileMapper.selectById(fileId);
        if (resourceFile == null || resourceFile.getDeleted() == 1) {
            throw new RuntimeException("文件不存在");
        }

        // 填充上传者信息
        User user = userMapper.selectById(resourceFile.getUserId());
        if (user != null) {
            resourceFile.setUploaderName(user.getRealName() != null ? user.getRealName() : user.getUsername());
        }

        // 格式化文件大小
        resourceFile.setFileSizeFormatted(formatFileSize(resourceFile.getFileSize()));

        // 分类名称
        resourceFile.setCategoryName(getCategoryName(resourceFile.getCategory()));

        return resourceFile;
    }

    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) {
        ResourceFile resourceFile = resourceFileMapper.selectById(fileId);
        if (resourceFile == null || resourceFile.getDeleted() == 1) {
            throw new RuntimeException("文件不存在");
        }

        // 使用 Paths 处理路径
        Path path = Paths.get(resourceFile.getFilePath());
        File file = path.toFile();
        
        if (!file.exists()) {
            throw new RuntimeException("文件已被删除");
        }

        // 增加下载次数
        incrementDownloadCount(fileId);

        // 设置响应头
        response.setContentType(resourceFile.getMimeType());
        response.setHeader("Content-Disposition", "attachment; filename=" + 
                URLEncoder.encode(resourceFile.getOriginalName(), StandardCharsets.UTF_8));
        response.setContentLengthLong(resourceFile.getFileSize());

        // 写入文件流
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<ResourceFile> getUserResources(IPage<ResourceFile> page, Long userId) {
        LambdaQueryWrapper<ResourceFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceFile::getUserId, userId);
        wrapper.eq(ResourceFile::getDeleted, 0);
        wrapper.orderByDesc(ResourceFile::getCreateTime);

        return resourceFileMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public boolean auditResource(Long fileId, Integer status) {
        ResourceFile resourceFile = new ResourceFile();
        resourceFile.setId(fileId);
        resourceFile.setStatus(status);
        return resourceFileMapper.updateById(resourceFile) > 0;
    }

    @Override
    public void incrementDownloadCount(Long fileId) {
        resourceFileMapper.incrementDownloadCount(fileId);
    }

    /**
     * 填充资源关联数据
     */
    private void fillResourceRelatedData(List<ResourceFile> resources) {
        if (resources == null || resources.isEmpty()) {
            return;
        }

        // 获取所有用户ID
        List<Long> userIds = resources.stream()
                .map(ResourceFile::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        // 填充数据
        for (ResourceFile resource : resources) {
            User user = userMap.get(resource.getUserId());
            if (user != null) {
                resource.setUploaderName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
            resource.setFileSizeFormatted(formatFileSize(resource.getFileSize()));
            resource.setCategoryName(getCategoryName(resource.getCategory()));
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String extension) {
        extension = extension.toLowerCase();
        if (Set.of("jpg", "jpeg", "png", "gif", "bmp").contains(extension)) {
            return "IMAGE";
        } else if (Set.of("mp4", "avi", "mov", "wmv").contains(extension)) {
            return "VIDEO";
        } else if (Set.of("doc", "docx", "pdf", "txt", "ppt", "pptx", "xls", "xlsx").contains(extension)) {
            return "DOCUMENT";
        }
        return "OTHER";
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return new DecimalFormat("#.00").format(size / 1024.0) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return new DecimalFormat("#.00").format(size / (1024.0 * 1024)) + " MB";
        } else {
            return new DecimalFormat("#.00").format(size / (1024.0 * 1024 * 1024)) + " GB";
        }
    }

    /**
     * 获取分类名称
     */
    private String getCategoryName(String category) {
        if (category == null) return "其他";
        switch (category) {
            case "TUTORIAL": return "魔术教程";
            case "SCRIPT": return "剧本资料";
            case "PROP": return "道具制作";
            case "PERFORMANCE": return "演出视频";
            default: return "其他";
        }
    }
}
