package cn.magicclub.service;

import cn.magicclub.entity.Developer;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 开发人员Service接口
 *
 * @author demo
 * @since 2025-04-13
 */
public interface DeveloperService extends IService<Developer> {

    /**
     * 添加开发人员
     */
    Developer addDeveloper(Developer developer);

    /**
     * 更新开发人员信息
     */
    boolean updateDeveloper(Developer developer);

    /**
     * 删除开发人员
     */
    boolean deleteDeveloper(Long id);

    /**
     * 获取开发人员详情
     */
    Developer getDeveloperDetail(Long id);

    /**
     * 获取所有在职开发人员
     */
    List<Developer> getActiveDevelopers();

    /**
     * 获取所有开发人员（包括离职）
     */
    List<Developer> getAllDevelopers();

    /**
     * 按状态获取开发人员
     */
    List<Developer> getDevelopersByStatus(Boolean isActive);

    /**
     * 设置开发人员在职状态
     */
    boolean setActiveStatus(Long id, Boolean isActive);
}
