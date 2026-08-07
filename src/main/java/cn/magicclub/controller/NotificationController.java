package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.Result;
import cn.magicclub.entity.Notification;
import cn.magicclub.entity.OperationLog;
import cn.magicclub.service.NotificationService;
import cn.magicclub.service.OperationLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知消息Controller
 *
 * @author demo
 * @since 2025-08-05
 */
@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;
    private final OperationLogService operationLogService;
    private final HttpServletRequest request;

    /**
     * 获取当前用户的消息列表（分页，按时间倒序）
     */
    @GetMapping("/list")
    public Result<IPage<Notification>> list(@RequestHeader("Authorization") String token,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        IPage<Notification> page = new Page<>(pageNum, pageSize);
        return Result.ok(notificationService.getNotificationList(page, userId));
    }

    /**
     * 获取当前用户未读消息数
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        return Result.ok(notificationService.getUnreadCount(userId));
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        boolean success = notificationService.markAsRead(id, userId);
        return success ? Result.ok("已标记为已读", true) : Result.error("操作失败");
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<Boolean> readAll(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        boolean success = notificationService.readAll(userId);
        return Result.ok("全部已读", success);
    }

    /**
     * 清空已读通知
     */
    @DeleteMapping("/clear-read")
    public Result<Boolean> clearRead(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        boolean success = notificationService.clearRead(userId);
        return Result.ok("已清空", success);
    }

    /**
     * 发送公告（仅管理员）
     * @param ossUrls 可选：前端直传 OSS 后的 URL 列表，绕过文件上传
     */
    @PostMapping("/announce")
    public Result<String> announce(@RequestHeader("Authorization") String token,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   @RequestParam(required = false) List<MultipartFile> files,
                                   @RequestParam(required = false) List<String> ossUrls) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");

        String role = getRoleFromToken(token);
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可发送公告");
        }

        if (title == null || title.trim().isEmpty()) {
            return Result.error("公告标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            return Result.error("公告内容不能为空");
        }

        try {
            notificationService.announce(title, content, null, files, ossUrls, userId);

            // 操作日志
            saveOperationLog(token, "SEND_ANNOUNCEMENT",
                    title,
                    String.valueOf(userId),
                    "发送公告: " + title);

            return Result.ok("公告发送成功");
        } catch (Exception e) {
            log.error("公告发送失败", e);
            return Result.error("公告发送失败: " + e.getMessage());
        }
    }

    /**
     * 撤回公告 - 批量撤回该公告的所有副本
     */
    @PutMapping("/{id}/recall")
    public Result<String> recall(@RequestHeader("Authorization") String token,
                                 @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");

        String role = getRoleFromToken(token);
        boolean isAdmin = "ADMIN".equals(role);

        String result = notificationService.recallNotification(id, userId, isAdmin);
        if ("ok".equals(result)) {
            // 操作日志
            saveOperationLog(token, "RECALL_ANNOUNCEMENT",
                    "公告ID: " + id,
                    String.valueOf(id),
                    "撤回公告");

            return Result.ok("公告已撤回");
        } else {
            return Result.error(result);
        }
    }

    /**
     * 上传文件（供公告图片/附件上传使用，不创建ResourceFile记录）
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestHeader("Authorization") String token,
                                              @RequestParam MultipartFile file) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");

        try {
            String url = notificationService.uploadFile(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", file.getOriginalFilename());
            return Result.ok(result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    private Long getUserIdFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) token = token.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    private String getRoleFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) token = token.substring(7);
        return jwtUtil.getRoleFromToken(token);
    }

    private void saveOperationLog(String token, String action, String target, String targetId, String detail) {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            String username = jwtUtil.getUsernameFromToken(token.replace("Bearer ", ""));

            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setAction(action);
            log.setTarget(target);
            log.setTargetId(targetId);
            log.setDetail(detail);
            log.setIp(getClientIp());
            operationLogService.addLog(log);
        } catch (Exception e) {
            // 日志记录失败不影响业务
        }
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
