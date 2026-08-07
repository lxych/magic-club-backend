package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.Result;
import cn.magicclub.entity.Memorial;
import cn.magicclub.entity.ResourceFile;
import cn.magicclub.service.MemorialService;
import cn.magicclub.service.ResourceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 纪念墙Controller
 *
 * @author demo
 * @since 2025-04-13
 */
@RestController
@RequestMapping("/memorial")
@RequiredArgsConstructor
public class MemorialController {

    private final MemorialService memorialService;
    private final ResourceFileService resourceFileService;
    private final JwtUtil jwtUtil;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 从token中获取用户ID
     */
    private Long getUserIdFromToken(String token) {
        if (token == null) return null;
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 创建历史事件
     */
    @PostMapping
    public Result<Memorial> createMemorial(@RequestHeader("Authorization") String token,
                                           @RequestBody Memorial memorial) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        Memorial created = memorialService.createMemorial(memorial, userId);
        return Result.ok("创建成功", created);
    }

    /**
     * 更新历史事件
     */
    @PutMapping("/{id}")
    public Result<Boolean> updateMemorial(@PathVariable Long id, @RequestBody Memorial memorial) {
        memorial.setId(id);
        boolean success = memorialService.updateMemorial(memorial);
        return Result.ok("更新成功", success);
    }

    /**
     * 删除历史事件（仅管理员）
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteMemorial(@RequestHeader("Authorization") String token,
                                          @PathVariable Long id) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean success = memorialService.deleteMemorial(id, userId);
        return Result.ok("删除成功", success);
    }

    /**
     * 获取历史事件详情
     */
    @GetMapping("/{id}")
    public Result<Memorial> getMemorialDetail(@PathVariable Long id) {
        Memorial memorial = memorialService.getMemorialDetail(id);
        return Result.ok(memorial);
    }

    /**
     * 按年份查询历史事件
     */
    @GetMapping("/year/{year}")
    public Result<List<Memorial>> getMemorialsByYear(@PathVariable Integer year) {
        List<Memorial> list = memorialService.getMemorialsByYear(year);
        return Result.ok(list);
    }

    /**
     * 获取所有年份列表
     */
    @GetMapping("/years")
    public Result<List<Integer>> getAllYears() {
        List<Integer> years = memorialService.getAllYears();
        return Result.ok(years);
    }

    /**
     * 获取按年份分组的历史事件数据
     */
    @GetMapping("/timeline")
    public Result<Map<Integer, List<Memorial>>> getMemorialsGroupByYear() {
        Map<Integer, List<Memorial>> result = memorialService.getMemorialsGroupByYear();
        return Result.ok(result);
    }

    /**
     * 上传纪念墙文件（图片、Word、PDF、Excel等）
     */
    @PostMapping("/upload")
    public Result<List<String>> uploadFiles(@RequestHeader("Authorization") String token,
                                            @RequestParam("files") List<MultipartFile> files) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        
        try {
            List<String> urls = files.stream()
                    .map(file -> resourceFileService.uploadFile(file, userId, "纪念墙附件", "memorial", 1))
                    .map(rf -> {
                        // 纪念墙图片自动审批，不进审批流
                        resourceFileService.lambdaUpdate()
                                .set(ResourceFile::getStatus, 1)
                                .eq(ResourceFile::getId, rf.getId())
                                .update();
                        return rf.getId();
                    })
                    .map(id -> "/api/resource/" + id + "/preview")
                    .collect(Collectors.toList());
            return Result.ok("上传成功", urls);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 按多个年份查询历史事件
     */
    @GetMapping("/timeline/batch")
    public Result<Map<Integer, List<Memorial>>> getMemorialsByYears(@RequestParam String years) {
        // years 格式: 2021,2022,2023
        Set<Integer> yearSet = java.util.Arrays.stream(years.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        Map<Integer, List<Memorial>> result = memorialService.getMemorialsByYears(yearSet);
        return Result.ok(result);
    }
}
