package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.PermissionConstants;
import cn.magicclub.common.Result;
import cn.magicclub.entity.ResourceFile;
import cn.magicclub.security.RequirePermission;
import cn.magicclub.service.ResourceFileService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 资源Controller
 */
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceFileService resourceFileService;
    private final JwtUtil jwtUtil;

    /**
     * 上传文件（所有登录用户）
     */
    @PostMapping("/upload")
    public Result<ResourceFile> uploadFile(@RequestHeader("Authorization") String token,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false, defaultValue = "1") Integer allowDownload) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        ResourceFile resourceFile = resourceFileService.uploadFile(file, userId, description, category, allowDownload);
        return Result.ok("上传成功，等待管理员审批", resourceFile);
    }

    /** 分页查询（已通过） */
    @GetMapping("/list")
    public Result<IPage<ResourceFile>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String keyword) {
        IPage<ResourceFile> page = new Page<>(pageNum, pageSize);
        return Result.ok(resourceFileService.getResourcePage(page, category, fileType, keyword));
    }

    /** 我的资源 */
    @GetMapping("/my")
    public Result<IPage<ResourceFile>> getMyResources(@RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        IPage<ResourceFile> page = new Page<>(pageNum, pageSize);
        return Result.ok(resourceFileService.getUserResources(page, userId));
    }

    /** 待审批列表（权限：resource:audit） */
    @GetMapping("/pending")
    @RequirePermission(PermissionConstants.RESOURCE_AUDIT)
    public Result<IPage<ResourceFile>> pendingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<ResourceFile> page = new Page<>(pageNum, pageSize);
        return Result.ok(resourceFileService.getResourcesByStatus(page, 0));
    }

    /** 按状态查全部资源（权限：resource:audit） */
    @GetMapping("/status/{status}")
    @RequirePermission(PermissionConstants.RESOURCE_AUDIT)
    public Result<IPage<ResourceFile>> listByStatus(
            @PathVariable Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<ResourceFile> page = new Page<>(pageNum, pageSize);
        return Result.ok(resourceFileService.getResourcesByStatus(page, status));
    }

    /** 论坛图片上传 */
    @PostMapping("/forum-upload")
    public Result<Map<String,String>> forumImageUpload(@RequestHeader("Authorization") String token,
                                                        @RequestParam("file") MultipartFile file) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        ResourceFile rf = resourceFileService.uploadFile(file, userId, "论坛图片", "GENERAL", 0);
        Map<String,String> map = new HashMap<>();
        map.put("fileUrl", rf.getFileUrl());
        return Result.ok("上传成功", map);
    }

    /** 资源详情 */
    @GetMapping("/{id}")
    public Result<ResourceFile> getDetail(@PathVariable Long id) {
        return Result.ok(resourceFileService.getResourceDetail(id));
    }

    /** 下载 */
    @GetMapping("/{id}/download")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) {
        resourceFileService.downloadFile(id, response);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFile(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error(401, "请先登录");
        return Result.ok("删除成功", resourceFileService.deleteFile(id, userId));
    }

    /** 审批（权限：resource:audit） */
    @PostMapping("/{id}/audit")
    @RequirePermission(PermissionConstants.RESOURCE_AUDIT)
    public Result<Boolean> auditResource(@RequestHeader("Authorization") String token,
            @PathVariable Long id, @RequestParam Integer status) {
        Long auditorId = getUserIdFromToken(token);
        return Result.ok("审核完成", resourceFileService.auditResource(id, status, auditorId, null));
    }

    /** 预览（inline，不触发下载） */
    @GetMapping("/{id}/preview")
    public void previewFile(@PathVariable Long id, HttpServletResponse response) {
        resourceFileService.previewFile(id, response);
    }

    private Long getUserIdFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) token = token.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }
}
