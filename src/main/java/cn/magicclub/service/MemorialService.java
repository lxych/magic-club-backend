package cn.magicclub.service;

import cn.magicclub.entity.Memorial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 纪念墙Service接口
 *
 * @author demo
 * @since 2025-04-13
 */
public interface MemorialService extends IService<Memorial> {

    /**
     * 创建历史事件
     */
    Memorial createMemorial(Memorial memorial, Long createBy);

    /**
     * 更新历史事件
     */
    boolean updateMemorial(Memorial memorial);

    /**
     * 删除历史事件（仅管理员）
     */
    boolean deleteMemorial(Long id, Long userId);

    /**
     * 获取历史事件详情
     */
    Memorial getMemorialDetail(Long id);

    /**
     * 按年份查询历史事件
     */
    List<Memorial> getMemorialsByYear(Integer year);

    /**
     * 获取所有年份列表
     */
    List<Integer> getAllYears();

    /**
     * 获取按年份分组的历史事件数据
     */
    Map<Integer, List<Memorial>> getMemorialsGroupByYear();

    /**
     * 获取多个年份的历史事件
     */
    Map<Integer, List<Memorial>> getMemorialsByYears(Set<Integer> years);
}
