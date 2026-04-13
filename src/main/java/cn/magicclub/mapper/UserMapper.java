package cn.magicclub.mapper;

import cn.magicclub.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper接口
 *
 * @author demo
 * @since 2025-04-13
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM mc_user WHERE username = #{username} AND deleted = 0")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据学号查询用户
     */
    @Select("SELECT * FROM mc_user WHERE student_no = #{studentNo} AND deleted = 0")
    User selectByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 根据用户名更新密码
     */
    @Update("UPDATE mc_user SET password = #{password} WHERE username = #{username}")
    void updatePasswordByUsername(@Param("username") String username, @Param("password") String password);
}
