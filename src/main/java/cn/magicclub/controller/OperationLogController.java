package cn.magicclub.controller;

import cn.magicclub.common.PermissionConstants;
import cn.magicclub.common.Result;
import cn.magicclub.entity.OperationLog;
import cn.magicclub.security.RequirePermission;
import cn.magicclub.service.OperationLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志 Controller
 * <p>
 * 权限：log:view（管理员默认拥有，可下放给指定成员）
 *
 * @author demo
 * @since 2025-08-06
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/logs")
    @RequirePermission(PermissionConstants.LOG_VIEW)
    public Result<IPage<OperationLog>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        IPage<OperationLog> result = operationLogService.page(page, action, username, startDate, endDate);
        return Result.ok(result);
    }
}
