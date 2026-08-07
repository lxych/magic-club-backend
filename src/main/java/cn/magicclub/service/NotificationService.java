package cn.magicclub.service;

import cn.magicclub.entity.Notification;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 通知消息Service接口
 *
 * @author demo
 * @since 2025-08-05
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 获取用户的通知列表（分页，按时间倒序）
     */
    IPage<Notification> getNotificationList(IPage<Notification> page, Long userId);

    /**
     * 获取用户未读消息数
     */
    long getUnreadCount(Long userId);

    /**
     * 标记单条通知为已读
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 标记用户所有通知为已读
     */
    boolean readAll(Long userId);

    /**
     * 清空用户所有已读通知
     */
    boolean clearRead(Long userId);

    /**
     * 创建通知
     */
    void createNotification(Long userId, String title, String content, String type, Long relatedId);

    /**
     * 发送公告（管理员向全员发送）
     * @param ossUrls 可选：前端直传 OSS 后的 URL 列表
     */
    void announce(String title, String content, String attachments, List<MultipartFile> files,
                  List<String> ossUrls, Long senderId);

    /**
     * 撤回公告
     * @return 撤回结果消息
     */
    String recallNotification(Long notificationId, Long userId, boolean isAdmin);

    /**
     * 上传文件（简单存储，不创建ResourceFile）
     * @return 文件URL
     */
    String uploadFile(MultipartFile file) throws Exception;
}
