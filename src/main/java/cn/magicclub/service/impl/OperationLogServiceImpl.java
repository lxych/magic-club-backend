package cn.magicclub.service.impl;

import cn.magicclub.entity.OperationLog;
import cn.magicclub.mapper.OperationLogMapper;
import cn.magicclub.service.OperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 操作日志 Service 实现
 *
 * @author demo
 * @since 2025-08-06
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void addLog(OperationLog log) {
        baseMapper.insert(log);
    }

    @Override
    public IPage<OperationLog> page(Page<OperationLog> page, String action, String username, String startDate, String endDate) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(action)) {
            wrapper.eq(OperationLog::getAction, action);
        }
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (StringUtils.hasText(startDate)) {
            try {
                wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(startDate + "T00:00:00"));
            } catch (Exception ignored) {
            }
        }
        if (StringUtils.hasText(endDate)) {
            try {
                wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(endDate + "T23:59:59"));
            } catch (Exception ignored) {
            }
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }
}
