package cn.magicclub.service;

import cn.magicclub.entity.Memorial;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 纪念墙Service接口
 *
 * @author demo
 * @since 2025-04-13
 */
public interface MemorialService extends IService<Memorial> {

    /**
     * 创建纪念墙记录
     */
    Memorial createMemorial(Memorial memorial);

    /**
     * 更新纪念墙记录
     */
    boolean updateMemorial(Memorial memorial);

    /**
     * 删除纪念墙记录
     */
    boolean deleteMemorial(Long id);

    /**
     * 获取纪念墙详情
     */
    Memorial getMemorialDetail(Long id);

    /**
     * 按年份查询纪念墙数据
     */
    List<Memorial> getMemorialsByYear(Integer year);

    /**
     * 获取所有毕业年份列表
     */
    List<Integer> getAllGraduationYears();

    /**
     * 获取按年份分组的纪念墙数据
     */
    Map<Integer, List<Memorial>> getMemorialsGroupByYear();

    /**
     * 将用户添加到纪念墙（毕业）
     */
    Memorial addToMemorial(Long userId, Integer graduationYear, String message);
}
