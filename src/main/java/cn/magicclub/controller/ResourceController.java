package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.Result;
import cn.magicclub.entity.ResourceFile;
import cn.magicclub.service.ResourceFileService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 资源Controller
 *
 * @author demo
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceFileService resourceFileService;
    private final JwtUtil jwtUtil;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<ResourceFile> uploadFile(@RequestHeader("Authorization") String token,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(required = false) String category) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        ResourceFile resourceFile = resourceFileService.uploadFile(file, userId, description, category);
        return Result.ok("上传成功", resourceFile);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFile(@RequestHeader("Authorization") String token,
                                      @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        boolean success = resourceFileService.deleteFile(id, userId);
        return Result.ok("删除成功", success);
    }

    /**
     * 分页查询资源列表
     */
    @GetMapping("/list")
    public Result<IPage<ResourceFile>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String keyword) {

        IPage<ResourceFile> page = new Page<>(pageNum, pageSize);
        IPage<ResourceFile> resultPage = resourceFileService.getResourcePage(page, category, fileType, keyword);
        return Result.ok(resultPage);
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/{id}")
    public Result<ResourceFile> getDetail(@PathVariable Long id) {
        ResourceFile resourceFile = resourceFileService.getResourceDetail(id);
        return Result.ok(resourceFile);
    }

    /**
     * 下载文件
     */
    @GetMapping("/{id}/download")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) {
        resourceFileService.downloadFile(id, response);
    }

    /**
     * 获取用户的资源列表
     */
    @GetMapping("/my")
    public Result<IPage<ResourceFile>> getMyResources(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        IPage<ResourceFile> page = new Page<>(pageNum, pageSize);
        IPage<ResourceFile> resultPage = resourceFileService.getUserResources(page, userId);
        return Result.ok(resultPage);
    }

    /**
     * 审核资源 (管理员功能)
     */
    @PostMapping("/{id}/audit")
    public Result<Boolean> auditResource(@PathVariable Long id,
                                          @RequestParam Integer status) {
        boolean success = resourceFileService.auditResource(id, status);
        return Result.ok("审核完成", success);
    }

    /**
     * 从Token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }
}
