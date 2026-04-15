package cn.magicclub.mapper;

import cn.magicclub.entity.InviteCode;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 邀请码Mapper
 */
@Mapper
public interface InviteCodeMapper extends BaseMapper<InviteCode> {

    /**
     * 获取当前有效的邀请码
     */
    @Select("SELECT * FROM mc_invite_code WHERE status = 1 ORDER BY create_time DESC LIMIT 1")
    InviteCode selectValidCode();
}
