package cn.magicclub.service.impl;

import cn.magicclub.entity.Developer;
import cn.magicclub.mapper.DeveloperMapper;
import cn.magicclub.service.DeveloperService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 开发人员Service实现类
 *
 * @author demo
 * @since 2025-04-13
 */
@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl extends ServiceImpl<DeveloperMapper, Developer> implements DeveloperService {

    private final DeveloperMapper developerMapper;

    @Override
    @Transactional
    public Developer addDeveloper(Developer developer) {
        // 设置默认值
        if (developer.getIsActive() == null) {
            developer.setIsActive(1);
        }
        if (developer.getDisplayOrder() == null) {
            developer.setDisplayOrder(0);
        }

        developerMapper.insert(developer);
        return developer;
    }

    @Override
    @Transactional
    public boolean updateDeveloper(Developer developer) {
        return developerMapper.updateById(developer) > 0;
    }

    @Override
    @Transactional
    public boolean deleteDeveloper(Long id) {
        return developerMapper.deleteById(id) > 0;
    }

    @Override
    public Developer getDeveloperDetail(Long id) {
        Developer developer = developerMapper.selectById(id);
        if (developer == null || developer.getDeleted() == 1) {
            throw new RuntimeException("开发人员不存在");
        }

        // 设置状态名称
        developer.setStatusName(developer.getIsActive() == 1 ? "在职" : "已离职");

        return developer;
    }

    @Override
    public List<Developer> getActiveDevelopers() {
        List<Developer> list = developerMapper.selectActiveDevelopers();
        fillStatusName(list);
        return list;
    }

    @Override
    public List<Developer> getAllDevelopers() {
        List<Developer> list = developerMapper.selectList(null);
        fillStatusName(list);
        return list;
    }

    @Override
    public List<Developer> getDevelopersByStatus(Boolean isActive) {
        List<Developer> list = developerMapper.selectByStatus(isActive ? 1 : 0);
        fillStatusName(list);
        return list;
    }

    @Override
    @Transactional
    public boolean setActiveStatus(Long id, Boolean isActive) {
        Developer developer = new Developer();
        developer.setId(id);
        developer.setIsActive(isActive ? 1 : 0);
        return developerMapper.updateById(developer) > 0;
    }

    /**
     * 填充状态名称
     */
    private void fillStatusName(List<Developer> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Developer developer : list) {
            developer.setStatusName(developer.getIsActive() == 1 ? "在职" : "已离职");
        }
    }
}
