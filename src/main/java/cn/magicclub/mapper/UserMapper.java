package cn.magicclub.mapper;

import cn.magicclub.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    /**
     * 查询毕业成员列表
     */
    @Select("<script>SELECT * FROM mc_user WHERE is_graduated = 1 AND deleted = 0 " +
            "<if test='graduationYear != null'>AND graduation_year = #{graduationYear}</if> " +
            "ORDER BY graduation_year DESC, id DESC</script>")
    IPage<User> selectGraduatedUsers(IPage<User> page, @Param("graduationYear") Integer graduationYear);

    /**
     * 查询所有启用的活跃用户（status=1, deleted=0）
     */
    @Select("SELECT * FROM mc_user WHERE status = 1 AND deleted = 0")
    java.util.List<User> findAllActiveUsers();
}
