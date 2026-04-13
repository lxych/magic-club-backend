package cn.magicclub.mapper;

import cn.magicclub.entity.Developer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 开发人员Mapper接口
 *
 * @author demo
 * @since 2025-04-13
 */
@Mapper
public interface DeveloperMapper extends BaseMapper<Developer> {

    /**
     * 查询在职的开发人员
     */
    @Select("SELECT * FROM mc_developer WHERE is_active = 1 AND deleted = 0 ORDER BY display_order ASC, create_time ASC")
    List<Developer> selectActiveDevelopers();

    /**
     * 按状态查询开发人员
     */
    @Select("SELECT * FROM mc_developer WHERE is_active = #{isActive} AND deleted = 0 ORDER BY display_order ASC, create_time ASC")
    List<Developer> selectByStatus(@Param("isActive") Integer isActive);
}
