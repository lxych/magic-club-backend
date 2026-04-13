package cn.magicclub.mapper;

import cn.magicclub.entity.Memorial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 纪念墙Mapper接口
 *
 * @author demo
 * @since 2025-04-13
 */
@Mapper
public interface MemorialMapper extends BaseMapper<Memorial> {

    /**
     * 按毕业年份查询纪念墙数据
     */
    @Select("SELECT * FROM mc_memorial WHERE graduation_year = #{year} AND deleted = 0 ORDER BY display_order ASC, create_time DESC")
    List<Memorial> selectByGraduationYear(@Param("year") Integer year);

    /**
     * 查询所有毕业年份列表
     */
    @Select("SELECT DISTINCT graduation_year FROM mc_memorial WHERE deleted = 0 ORDER BY graduation_year DESC")
    List<Integer> selectAllGraduationYears();
}
