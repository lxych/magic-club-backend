package cn.magicclub.mapper;

import cn.magicclub.entity.UserToken;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Token Mapper
 */
public interface UserTokenMapper extends BaseMapper<UserToken> {

    /**
     * 查询有效 Token
     */
    @Select("SELECT * FROM mc_user_token WHERE token = #{token} AND status = 1")
    UserToken selectValidByToken(@Param("token") String token);

    /**
     * 删除用户所有 Token
     */
    @Delete("DELETE FROM mc_user_token WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
