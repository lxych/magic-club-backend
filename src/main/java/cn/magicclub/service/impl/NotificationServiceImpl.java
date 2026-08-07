package cn.magicclub.service.impl;

import cn.magicclub.entity.Notification;
import cn.magicclub.entity.User;
import cn.magicclub.mapper.NotificationMapper;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.service.NotificationService;
import cn.magicclub.service.OssService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 通知消息Service实现类
 *
 * @author demo
 * @since 2025-08-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final OssService ossService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public IPage<Notification> getNotificationList(IPage<Notification> page, Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.orderByDesc(Notification::getCreateTime);
        IPage<Notification> result = notificationMapper.selectPage(page, wrapper);

        // 处理已撤回消息：内容改为提示，附件隐藏
        for (Notification n : result.getRecords()) {
            if (n.getIsRecalled() != null && n.getIsRecalled() == 1) {
                n.setContent("此消息已被撤回");
                n.setAttachments(null);
            }
        }
        return result;
    }

    @Override
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        wrapper.eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }
        notification.setIsRead(1);
        return notificationMapper.updateById(notification) > 0;
    }

    @Override
    @Transactional
    public boolean readAll(Long userId) {
        return notificationMapper.readAll(userId) > 0;
    }

    @Override
    public boolean clearRead(Long userId) {
        return notificationMapper.deleteReadByUserId(userId) > 0;
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String title, String content, String type, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRelatedId(relatedId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Transactional
    public void announce(String title, String content, String attachmentsJson, List<MultipartFile> files,
                         List<String> ossUrls, Long senderId) {
        // 1. 处理文件上传
        StringBuilder attachmentsBuilder = new StringBuilder();
        if (attachmentsJson != null && !attachmentsJson.isEmpty()) {
            attachmentsBuilder.append(attachmentsJson);
        }

        if (files != null && !files.isEmpty()) {
            List<String> fileAttachments = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                try {
                    String savedPath = saveUploadFile(file);
                    fileAttachments.add("{\"name\":\"" + escapeJson(file.getOriginalFilename()) +
                            "\",\"url\":\"" + savedPath +
                            "\",\"type\":\"" + file.getContentType() + "\"}");
                } catch (IOException e) {
                    log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                    throw new RuntimeException("文件上传失败: " + file.getOriginalFilename());
                }
            }
            if (!fileAttachments.isEmpty()) {
                if (attachmentsBuilder.length() > 0) {
                    // 合并已有JSON数组和新文件
                    String existing = attachmentsBuilder.toString().trim();
                    if (existing.startsWith("[") && existing.endsWith("]")) {
                        String inner = existing.substring(1, existing.length() - 1);
                        if (!inner.isEmpty()) {
                            attachmentsBuilder = new StringBuilder("[" + inner + "," + String.join(",", fileAttachments) + "]");
                        } else {
                            attachmentsBuilder = new StringBuilder("[" + String.join(",", fileAttachments) + "]");
                        }
                    }
                } else {
                    attachmentsBuilder = new StringBuilder("[" + String.join(",", fileAttachments) + "]");
                }
            }
        }

        // 1.5 处理 OSS 直传 URL（前端已上传到 OSS，无需后端再上传）
        if (ossUrls != null && !ossUrls.isEmpty()) {
            List<String> ossAttachments = new ArrayList<>();
            for (String ossUrl : ossUrls) {
                if (ossUrl == null || ossUrl.isEmpty()) continue;
                // 从 URL 中提取文件名
                String fileName = ossUrl.substring(ossUrl.lastIndexOf('/') + 1);
                // URL 解码并重建 attachment
                ossAttachments.add("{\"name\":\"" + escapeJson(fileName) +
                        "\",\"url\":\"" + escapeJson(ossUrl) +
                        "\",\"type\":\"application/octet-stream\"}");
            }
            if (!ossAttachments.isEmpty()) {
                if (attachmentsBuilder.length() > 0) {
                    String existing = attachmentsBuilder.toString().trim();
                    if (existing.startsWith("[") && existing.endsWith("]")) {
                        String inner = existing.substring(1, existing.length() - 1);
                        if (!inner.isEmpty()) {
                            attachmentsBuilder = new StringBuilder("[" + inner + "," + String.join(",", ossAttachments) + "]");
                        } else {
                            attachmentsBuilder = new StringBuilder("[" + String.join(",", ossAttachments) + "]");
                        }
                    }
                } else {
                    attachmentsBuilder = new StringBuilder("[" + String.join(",", ossAttachments) + "]");
                }
            }
        }

        String finalAttachments = attachmentsBuilder.length() > 0 ? attachmentsBuilder.toString() : null;

        // 2. 查询所有活跃用户
        List<User> activeUsers = userMapper.findAllActiveUsers();
        if (activeUsers.isEmpty()) {
            log.warn("没有活跃用户，公告发送跳过");
            return;
        }

        // 3. 向每个活跃用户插入一条公告通知
        LocalDateTime canRecallUntil = LocalDateTime.now().plusMinutes(2);

        for (User user : activeUsers) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setSenderId(senderId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("ANNOUNCEMENT");
            notification.setAttachments(finalAttachments);
            notification.setCanRecallUntil(canRecallUntil);
            notification.setIsRecalled(0);
            notification.setIsRead(0);
            notificationMapper.insert(notification);
        }

        log.info("公告已发送给 {} 位用户: title={}", activeUsers.size(), title);
    }

    @Override
    @Transactional
    public String recallNotification(Long notificationId, Long userId, boolean isAdmin) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            return "通知不存在";
        }
        if (!"ANNOUNCEMENT".equals(notification.getType())) {
            return "只能撤回公告消息";
        }
        if (notification.getIsRecalled() != null && notification.getIsRecalled() == 1) {
            return "该公告已被撤回";
        }
        if (notification.getCanRecallUntil() != null && notification.getCanRecallUntil().isBefore(LocalDateTime.now())) {
            return "已超过撤回时限（2分钟）";
        }

        // 管理员可撤回任意公告，普通用户只能撤回自己发的
        if (!isAdmin && !notification.getSenderId().equals(userId)) {
            return "您只能撤回自己发送的公告";
        }

        // 批量撤回该公告的所有副本（同一发送者+标题+内容的公告）
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getSenderId, notification.getSenderId())
               .eq(Notification::getTitle, notification.getTitle())
               .eq(Notification::getType, "ANNOUNCEMENT")
               .eq(Notification::getIsRecalled, 0);
        wrapper.set(Notification::getIsRecalled, 1);
        wrapper.set(Notification::getContent, "此消息已被撤回");
        int count = notificationMapper.update(null, wrapper);

        log.info("公告已批量撤回: {} 条, senderId={}, title={}", count, notification.getSenderId(), notification.getTitle());
        return "ok";
    }

    /**
     * 保存上传文件到 uploads 目录（不创建 ResourceFile 记录）
     * 大文件（超过阈值）上传到 OSS
     */
    private String saveUploadFile(MultipartFile file) throws IOException {
        // 大文件走 OSS
        if (file.getSize() > ossService.getFileSizeThreshold()) {
            return ossService.uploadFile(file);
        }

        // 确保上传目录存在
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path targetPath = uploadDir.resolve(fileName);
        file.transferTo(targetPath.toFile());

        return "/uploads/" + fileName;
    }

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        return saveUploadFile(file);
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
