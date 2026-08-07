package cn.magicclub.controller;

import cn.magicclub.common.JwtUtil;
import cn.magicclub.common.Result;
import cn.magicclub.entity.OperationLog;
import cn.magicclub.service.OperationLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志 Controller（仅管理员）
 *
 * @author demo
 * @since 2025-08-06
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;
    private final JwtUtil jwtUtil;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/logs")
    public Result<IPage<OperationLog>> list(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        String role = jwtUtil.getRoleFromToken(token.replace("Bearer ", ""));
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可查看操作日志");
        }

        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        IPage<OperationLog> result = operationLogService.page(page, action, username, startDate, endDate);
        return Result.ok(result);
    }
}
