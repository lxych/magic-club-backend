package cn.magicclub.service.impl;

import cn.magicclub.entity.Memorial;
import cn.magicclub.entity.User;
import cn.magicclub.mapper.MemorialMapper;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.service.MemorialService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 纪念墙Service实现类
 *
 * @author demo
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class MemorialServiceImpl extends ServiceImpl<MemorialMapper, Memorial> implements MemorialService {

    private final MemorialMapper memorialMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public Memorial createMemorial(Memorial memorial, Long createBy) {
        memorial.setCreateBy(createBy);
        
        // 设置默认值
        if (memorial.getDisplayOrder() == null) {
            memorial.setDisplayOrder(0);
        }
        if (memorial.getYear() == null && memorial.getEventDate() != null) {
            memorial.setYear(memorial.getEventDate().getYear());
        }

        memorialMapper.insert(memorial);
        return memorial;
    }

    @Override
    @Transactional
    public boolean updateMemorial(Memorial memorial) {
        // 如果更新了事件日期，同步更新年份
        if (memorial.getEventDate() != null) {
            memorial.setYear(memorial.getEventDate().getYear());
        }
        return memorialMapper.updateById(memorial) > 0;
    }

    @Override
    @Transactional
    public boolean deleteMemorial(Long id, Long userId) {
        // 查询纪念墙记录
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null) {
            throw new RuntimeException("记录不存在");
        }

        // 检查权限：仅管理员可删除
        User user = userMapper.selectById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("仅管理员可删除");
        }

        return memorialMapper.deleteById(id) > 0;
    }

    @Override
    public Memorial getMemorialDetail(Long id) {
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null || memorial.getDeleted() == 1) {
            throw new RuntimeException("记录不存在");
        }

        fillMemorialRelatedData(Collections.singletonList(memorial));
        return memorial;
    }

    @Override
    public List<Memorial> getMemorialsByYear(Integer year) {
        LambdaQueryWrapper<Memorial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Memorial::getYear, year);
        wrapper.eq(Memorial::getDeleted, 0);
        wrapper.orderByDesc(Memorial::getEventDate);
        
        List<Memorial> list = memorialMapper.selectList(wrapper);
        fillMemorialRelatedData(list);
        return list;
    }

    @Override
    public List<Integer> getAllYears() {
        LambdaQueryWrapper<Memorial> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Memorial::getYear);
        wrapper.eq(Memorial::getDeleted, 0);
        wrapper.groupBy(Memorial::getYear);
        wrapper.orderByDesc(Memorial::getYear);
        
        return memorialMapper.selectObjs(wrapper).stream()
                .map(o -> (Integer) o)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<Memorial>> getMemorialsGroupByYear() {
        // 获取所有年份
        List<Integer> years = getAllYears();

        Map<Integer, List<Memorial>> result = new LinkedHashMap<>();

        for (Integer year : years) {
            List<Memorial> list = getMemorialsByYear(year);
            if (!list.isEmpty()) {
                result.put(year, list);
            }
        }

        return result;
    }

    @Override
    public Map<Integer, List<Memorial>> getMemorialsByYears(Set<Integer> years) {
        Map<Integer, List<Memorial>> result = new LinkedHashMap<>();
        
        for (Integer year : years) {
            List<Memorial> list = getMemorialsByYear(year);
            if (!list.isEmpty()) {
                result.put(year, list);
            }
        }
        
        return result;
    }

    /**
     * 填充纪念墙关联数据
     */
    private void fillMemorialRelatedData(List<Memorial> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 获取所有创建者ID
        List<Long> userIds = list.stream()
                .map(Memorial::getCreateBy)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户信息
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        // 填充数据
        for (Memorial memorial : list) {
            User user = userMap.get(memorial.getCreateBy());
            if (user != null) {
                memorial.setCreateByName(user.getRealName() != null ? user.getRealName() : user.getUsername());
            }
        }
    }
}
