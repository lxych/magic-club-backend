package cn.magicclub.mapper;

import cn.magicclub.entity.SystemConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 系统配置Mapper
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据key获取配置
     */
    @Select("SELECT * FROM mc_system_config WHERE config_key = #{key}")
    SystemConfig selectByKey(String key);
}
