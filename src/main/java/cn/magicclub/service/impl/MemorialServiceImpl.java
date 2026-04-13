package cn.magicclub.service.impl;

import cn.magicclub.entity.Memorial;
import cn.magicclub.entity.User;
import cn.magicclub.mapper.MemorialMapper;
import cn.magicclub.mapper.UserMapper;
import cn.magicclub.service.MemorialService;
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
    public Memorial createMemorial(Memorial memorial) {
        // 检查用户是否已存在纪念墙
        Memorial existing = baseMapper.selectById(memorial.getUserId());
        if (existing != null && existing.getDeleted() == 0) {
            throw new RuntimeException("该用户已在纪念墙中");
        }

        // 设置默认值
        if (memorial.getDisplayOrder() == null) {
            memorial.setDisplayOrder(0);
        }
        if (memorial.getContactPublic() == null) {
            memorial.setContactPublic(1);
        }

        memorialMapper.insert(memorial);

        // 更新用户的毕业状态
        User user = new User();
        user.setId(memorial.getUserId());
        user.setIsGraduated(1);
        user.setGraduationYear(memorial.getGraduationYear());
        userMapper.updateById(user);

        return memorial;
    }

    @Override
    @Transactional
    public boolean updateMemorial(Memorial memorial) {
        return memorialMapper.updateById(memorial) > 0;
    }

    @Override
    @Transactional
    public boolean deleteMemorial(Long id) {
        // 查询纪念墙记录
        Memorial memorial = memorialMapper.selectById(id);
        if (memorial == null) {
            throw new RuntimeException("记录不存在");
        }

        // 更新用户的毕业状态
        User user = new User();
        user.setId(memorial.getUserId());
        user.setIsGraduated(0);
        userMapper.updateById(user);

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
        List<Memorial> list = memorialMapper.selectByGraduationYear(year);
        fillMemorialRelatedData(list);
        return list;
    }

    @Override
    public List<Integer> getAllGraduationYears() {
        return memorialMapper.selectAllGraduationYears();
    }

    @Override
    public Map<Integer, List<Memorial>> getMemorialsGroupByYear() {
        // 获取所有年份
        List<Integer> years = getAllGraduationYears();

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
    @Transactional
    public Memorial addToMemorial(Long userId, Integer graduationYear, String message) {
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        Memorial memorial = new Memorial();
        memorial.setUserId(userId);
        memorial.setGraduationYear(graduationYear);
        memorial.setMessage(message);
        memorial.setAchievements("");
        memorial.setCurrentStatus("");
        memorial.setDisplayOrder(0);
        memorial.setContactPublic(1);

        return createMemorial(memorial);
    }

    /**
     * 填充纪念墙关联数据
     */
    private void fillMemorialRelatedData(List<Memorial> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 获取所有用户ID
        List<Long> userIds = list.stream()
                .map(Memorial::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (k1, k2) -> k1));

        // 填充数据
        for (Memorial memorial : list) {
            User user = userMap.get(memorial.getUserId());
            if (user != null) {
                memorial.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                memorial.setUserAvatar(user.getAvatar());
                memorial.setDepartment(user.getDepartment());
                memorial.setMajor(user.getMajor());

                // 根据设置决定是否显示联系方式
                if (memorial.getContactPublic() == 1) {
                    memorial.setPhone(user.getPhone());
                    memorial.setEmail(user.getEmail());
                }
            }
        }
    }
}
