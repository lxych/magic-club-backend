package cn.magicclub.service;

import cn.magicclub.entity.OperationLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 操作日志 Service
 *
 * @author demo
 * @since 2025-08-06
 */
public interface OperationLogService {

    /**
     * 保存操作日志
     */
    void addLog(OperationLog log);

    /**
     * 分页查询操作日志（支持筛选）
     */
    IPage<OperationLog> page(Page<OperationLog> page, String action, String username, String startDate, String endDate);
}
